import React, { useRef, useState } from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface LogoUploadCardProps {
  titulo: string;
  subtitulo: string;
  variante: 'claro' | 'escuro';
  logoUrl: string;
  temLogo: boolean;
  onUpload: (arquivo: File) => Promise<void>;
  isUploading: boolean;
}

export const LogoUploadCard: React.FC<LogoUploadCardProps> = ({
  titulo,
  subtitulo,
  variante,
  logoUrl,
  temLogo,
  onUpload,
  isUploading,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadSuccess, setUploadSuccess] = useState(false);

  const isFundoEscuro = variante === 'escuro';
  const displaySrc = previewUrl || (temLogo ? `${logoUrl}?t=${Date.now()}` : null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
      setUploadSuccess(false);
    }
  };

  const handleSalvar = async () => {
    if (!selectedFile) return;
    try {
      await onUpload(selectedFile);
      setSelectedFile(null);
      setUploadSuccess(true);
      setTimeout(() => setUploadSuccess(false), 4000);
    } catch {
      // Erro tratado pelo toast global / mutation
    }
  };

  return (
    <Card variant="outlined" sx={{ borderRadius: 2, height: '100%' }}>
      <CardContent>
        <Stack spacing={2}>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              {titulo}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {subtitulo}
            </Typography>
          </Box>

          <Box
            sx={{
              height: 120,
              backgroundColor: isFundoEscuro ? '#121212' : '#FFFFFF',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 1.5,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              p: 1.5,
            }}
          >
            {displaySrc ? (
              <Box
                component="img"
                src={displaySrc}
                alt={titulo}
                sx={{
                  maxHeight: '100%',
                  maxWidth: '100%',
                  objectFit: 'contain',
                }}
              />
            ) : (
              <Typography variant="caption" sx={{ color: isFundoEscuro ? '#A0A0A0' : '#888888' }}>
                Nenhum logotipo cadastrado
              </Typography>
            )}
          </Box>

          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept="image/png,image/jpeg,image/webp,image/svg+xml"
              style={{ display: 'none' }}
            />
            <Button
              variant="outlined"
              size="small"
              startIcon={<CloudUploadIcon />}
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
            >
              Escolher Imagem
            </Button>
            {selectedFile && (
              <Button
                variant="contained"
                size="small"
                onClick={handleSalvar}
                disabled={isUploading}
                startIcon={isUploading ? <CircularProgress size={16} color="inherit" /> : undefined}
              >
                {isUploading ? 'Enviando...' : 'Salvar'}
              </Button>
            )}
            {uploadSuccess && (
              <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center', color: 'success.main' }}>
                <CheckCircleIcon fontSize="small" />
                <Typography variant="caption" sx={{ fontWeight: 600 }}>
                  Salvo!
                </Typography>
              </Stack>
            )}
          </Stack>
          {selectedFile && (
            <Typography variant="caption" color="text.secondary" noWrap>
              Arquivo: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)
            </Typography>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
};
