import React, { useState } from 'react';
import { TextField } from '@mui/material';
import SearchableAutocomplete from '@/shared/presentation/components/SearchableAutocomplete';
import { PessoaOpcao } from '@/contextos/conselho/domain/ata';
import { TERMO_MINIMO_BUSCA, useBuscaFieis } from '@/contextos/conselho/presentation/hooks/useAtas';

interface AtaBuscaFielProps {
  tipo: string;
  organizacaoId: string;
  onEscolher: (pessoa: PessoaOpcao) => void;
}

/**
 * Busca de qualquer fiel ativo pelo nome, para as reuniões de presença livre, em que a presença não se limita aos
 * membros do órgão. A busca é no servidor: a lista de fiéis da paróquia nunca é carregada inteira na tela, e só o
 * nome trafega (minimização de dados). Escolher uma pessoa a entrega a quem chama e limpa o campo.
 */
export const AtaBuscaFiel: React.FC<AtaBuscaFielProps> = ({ tipo, organizacaoId, onEscolher }) => {
  const [digitado, setDigitado] = useState('');
  const { data = [], isFetching } = useBuscaFieis(tipo, organizacaoId, digitado);
  const curto = digitado.trim().length < TERMO_MINIMO_BUSCA;

  return (
    <SearchableAutocomplete<PessoaOpcao>
      value={null}
      options={curto ? [] : data}
      loading={isFetching && !curto}
      inputValue={digitado}
      onInputChange={(_, texto, motivo) => motivo === 'input' && setDigitado(texto)}
      onChange={(_, pessoa) => {
        if (!pessoa) return;
        onEscolher(pessoa);
        setDigitado('');
      }}
      getOptionLabel={(pessoa) => pessoa.nome}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      filterOptions={(opcoes) => opcoes}
      noOptionsText={curto ? `Digite ao menos ${TERMO_MINIMO_BUSCA} letras do nome` : 'Nenhum fiel encontrado'}
      renderInput={(params) => <TextField {...params} label="Buscar fiel pelo nome" />}
    />
  );
};

export default AtaBuscaFiel;
