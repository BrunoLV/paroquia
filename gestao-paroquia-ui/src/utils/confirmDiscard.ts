/** Confirma descarte antes de fechar um formulário com alterações não salvas (`formState.isDirty`). */
export const confirmDiscard = (isDirty: boolean, onClose: () => void): void => {
  if (!isDirty || window.confirm('Existem alterações não salvas. Deseja realmente sair sem salvar?')) {
    onClose();
  }
};
