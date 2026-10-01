import ts from 'typescript';

/** Ações de linha usam menu compacto; botões textuais continuam permitidos fora de tabelas. */
function violations(code: string, file: string): string[] {
  const source = ts.createSourceFile(file, code, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
  const imported = new Map<string, string>();
  for (const statement of source.statements) {
    if (!ts.isImportDeclaration(statement) || !ts.isStringLiteral(statement.moduleSpecifier)) continue;
    if (statement.importClause?.name) imported.set(statement.importClause.name.text, statement.moduleSpecifier.text.split('/').pop() ?? '');
    const bindings = statement.importClause?.namedBindings;
    if (bindings && ts.isNamedImports(bindings)) for (const binding of bindings.elements) imported.set(binding.name.text, (binding.propertyName ?? binding.name).text);
  }
  const issues: string[] = [];
  function walk(node: ts.Node) {
    if (ts.isJsxSelfClosingElement(node) || ts.isJsxOpeningElement(node)) {
      const name = imported.get(node.tagName.getText(source));
      if (name && ['ActionButton', 'Button', 'IconButton', 'IconActionButton'].includes(name)) {
        let parent: ts.Node | undefined = node.parent;
        while (parent && !(ts.isJsxElement(parent) && imported.get(parent.openingElement.tagName.getText(source)) === 'TableCell')) parent = parent.parent;
        if (parent) issues.push(`${file}: ação de linha fora de TableActionMenu`);
      }
    }
    ts.forEachChild(node, walk);
  }
  walk(source);
  return issues;
}

it('detecta botões de linha com alias e permite botões de página', () => {
  const imported = "import Acao from '@/shared/presentation/components/ActionButton'; import {TableCell as Cell} from '@mui/material';";
  expect(violations(`${imported} const ui = <Cell><Acao/></Cell>;`, 'fixture.tsx')).toHaveLength(1);
  expect(violations(`${imported} const ui = <Acao/>;`, 'fixture.tsx')).toEqual([]);
});

it('todas as ações de linha usam o menu compacto compartilhado', () => {
  const files = import.meta.glob<string>('/src/contextos/**/*.tsx', { query: '?raw', import: 'default', eager: true });
  expect(Object.entries(files).filter(([file]) => !file.includes('.test.'))
    .flatMap(([file, code]) => violations(code, file))).toEqual([]);
});
