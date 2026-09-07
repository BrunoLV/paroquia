import {type ReactNode, type ReactElement} from 'react';
import {Box, Typography, useTheme} from '@mui/material';

interface PageHeaderProps {
  icon: ReactElement;
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

export default function PageHeader({icon, title, subtitle, actions}: PageHeaderProps) {
  const theme = useTheme();

  return (
    <Box sx={{ mb: 3, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 1.5 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
        <Box sx={{ p: 1, bgcolor: theme.custom?.headerIconBg ?? 'action.selected', borderRadius: 2, display: 'flex', color: 'primary.main' }}>
          {icon}
        </Box>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.02em', lineHeight: 1.2 }}>
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
      </Box>
      {actions}
    </Box>
  );
}
