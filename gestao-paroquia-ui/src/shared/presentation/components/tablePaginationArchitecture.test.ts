import ts from 'typescript';

/** Impede regressões de posição: o componente compartilhado é o tfoot, não um painel externo. */
function violations(code: string, file: string): string[] {
  const source = ts.createSourceFile(file, code, ts.ScriptTarget.Latest, true, ts.ScriptKind.TSX);
  const paginations = new Set<string>();
  for (const statement of source.statements) {
    if (ts.isImportDeclaration(statement) && ts.isStringLiteral(statement.moduleSpecifier)
      && statement.moduleSpecifier.text.endsWith('/SearchableTablePagination') && statement.importClause?.name) {
      paginations.add(statement.importClause.name.text);
    }
  }
  const issues: string[] = [];
  function walk(node: ts.Node) {
    if (ts.isJsxSelfClosingElement(node) && paginations.has(node.tagName.getText(source))) {
      const parent = node.parent;
      const insideTable = ts.isJsxElement(parent) && parent.openingElement.tagName.getText(source) === 'Table';
      const spansColumns = node.attributes.properties.some((attribute) => ts.isJsxAttribute(attribute)
        && attribute.name.getText(source) === 'colSpan');
      if (!insideTable || !spansColumns) issues.push(`${file}: paginação fora do rodapé ou sem colSpan`);
    }
    ts.forEachChild(node, walk);
  }
  walk(source);
  return issues;
}

it('detecta paginação externa mesmo com alias e exige cobertura das colunas', () => {
  const imported = "import Pages from '@/shared/presentation/components/SearchableTablePagination';";
  expect(violations(`${imported} const ui = <><Table/><Pages colSpan={3}/></>;`, 'fixture.tsx')).toHaveLength(1);
  expect(violations(`${imported} const ui = <Table><Pages/></Table>;`, 'fixture.tsx')).toHaveLength(1);
  expect(violations(`${imported} const ui = <Table><Pages colSpan={3}/></Table>;`, 'fixture.tsx')).toEqual([]);
});

it('todas as paginações residem dentro da respectiva tabela', () => {
  const files = import.meta.glob<string>('/src/**/*.tsx', { query: '?raw', import: 'default', eager: true });
  expect(Object.entries(files).filter(([file]) => !file.includes('.test.'))
    .flatMap(([file, code]) => violations(code, file))).toEqual([]);
});
