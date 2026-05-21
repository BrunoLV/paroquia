import React from 'react';
import {
  Grid,
  Typography,
  Box,
  Stack,
} from '@mui/material';
import SummaryCards from '../components/dashboard/SummaryCards';
import UpcomingEvents from '../components/dashboard/UpcomingEvents';
import QuickActions from '../components/dashboard/QuickActions';
import PendingApprovals from '../components/dashboard/PendingApprovals';

const Dashboard = () => {
  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 4, fontWeight: 700, color: '#37474f' }}>
        Dashboard
      </Typography>

      <Grid container spacing={3}>
        {/* Summary Cards Row */}
        <Grid size={{ xs: 12 }}>
          <Typography variant="h6" sx={{ mb: 2, fontWeight: 600 }}>
            Visão Geral
          </Typography>
          <SummaryCards />
        </Grid>

        {/* Main Content Areas */}
        <Grid size={{ xs: 12, md: 8 }}>
          <UpcomingEvents />
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Stack spacing={3}>
            <QuickActions />
            <PendingApprovals />
          </Stack>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
