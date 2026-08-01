import {type ReactNode, type ReactElement} from 'react';
import {Box, Typography} from '@mui/material';
import theme from '../theme';

interface PageHeaderProps {
  icon: ReactElement;
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

export default function PageHeader({icon, title, subtitle, actions}: PageHeaderProps) {
  return (
    <Box sx={{ mb: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box sx={{ p: 1.5, bgcolor: theme.custom.headerIconBg, display: 'flex' }}>
          {icon}
        </Box>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.5px' }}>
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
