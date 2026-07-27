declare module '@dabeng/react-orgchart' {
  import { ComponentType, ReactNode } from 'react';

  export interface OrganizationChartProps {
    datasource: any;
    pan?: boolean;
    zoom?: boolean;
    chartClass?: string;
    NodeTemplate?: ComponentType<{ nodeData: any }>;
    draggable?: boolean;
    collapsible?: boolean;
    multipleSelect?: boolean;
    onClickNode?: (nodeData: any) => void;
    children?: ReactNode;
  }

  const OrganizationChart: ComponentType<OrganizationChartProps>;
  export default OrganizationChart;
}
