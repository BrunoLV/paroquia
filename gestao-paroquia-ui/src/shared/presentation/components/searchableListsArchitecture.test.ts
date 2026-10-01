import ts from 'typescript';

/** A regra cobre aliases e componentes embutidos; MenuItem pode descrever opções fechadas. */
function violations(code: string, file: string): string[] {
  const source = ts.createSourceFile(file, code, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
  const imported = new Map<string, string>();
  const issues: string[] = [];
  for (const statement of source.statements) {
    if (!ts.isImportDeclaration(statement) || !ts.isStringLiteral(statement.moduleSpecifier)) continue;
    const module = statement.moduleSpecifier.text;
    if (!module.startsWith('@mui/material')) continue;
    const clause = statement.importClause;
    if (clause?.name) imported.set(clause.name.text, module.split('/').pop() ?? '');
    const bindings = clause?.namedBindings;
    if (bindings && ts.isNamedImports(bindings)) {
      for (const binding of bindings.elements) imported.set(binding.name.text, (binding.propertyName ?? binding.name).text);
    }
    if (bindings && ts.isNamespaceImport(bindings)) imported.set(bindings.name.text, '*');
  }
  function walk(node: ts.Node) {
    if (ts.isJsxOpeningElement(node) || ts.isJsxSelfClosingElement(node)) {
      const tag = node.tagName.getText(source);
      const [namespace, member] = tag.split('.');
      const name = imported.get(namespace) === '*' ? member : imported.get(tag);
      const selectAttribute = node.attributes.properties.some((attribute) => ts.isJsxAttribute(attribute) && attribute.name.getText(source) === 'select');
      if (tag === 'select' || name === 'Select' || (name === 'TextField' && selectAttribute)) issues.push(`${file}: lista sem pesquisa`);
      if (name === 'Autocomplete' && !file.endsWith('/SearchableAutocomplete.tsx')) issues.push(`${file}: autocomplete fora do padrão compartilhado`);
      if (name === 'TablePagination' && !file.endsWith('/SearchableTablePagination.tsx')) issues.push(`${file}: paginação sem seletor pesquisável`);
    }
    ts.forEachChild(node, walk);
  }
  walk(source);
  return issues;
}

describe('governança de listas do Design System', () => {
  it('detecta aliases, imports diretos e seletores HTML', () => {
    expect(violations("import {Select as Lista, TextField as Campo, TablePagination as Pages} from '@mui/material'; const ui = <><Lista/><Campo select/><Pages/><select/></>;", '/feature.tsx')).toHaveLength(4);
    expect(violations("import Lista from '@mui/material/Select'; const ui = <Lista/>;", '/feature.tsx')).toHaveLength(1);
    expect(violations("import * as M from '@mui/material'; const ui = <M.Select/>;", '/feature.tsx')).toHaveLength(1);
  });

  it('todas as telas usam listas com pesquisa e paginação padronizada', () => {
    const files = import.meta.glob<string>('/src/**/*.tsx', { query: '?raw', import: 'default', eager: true });
    const issues = Object.entries(files).filter(([file]) => !file.includes('.test.'))
      .flatMap(([file, code]) => violations(code, file));
    expect(issues).toEqual([]);
  });
});
