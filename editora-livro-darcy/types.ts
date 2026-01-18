import { ReactNode } from 'react';

export type PageLayout = 'standard' | 'cover' | 'toc';

export interface MarginNoteData {
  id: string;
  type: 'note' | 'tip' | 'warning' | 'link' | 'quote';
  title?: string;
  content: string;
}

export interface PageContent {
  id: string;
  title: string;
  module: string; // Ex: "Módulo 01", "Capa"
  pageNumber: number;
  layout: PageLayout;
  htmlContent?: string; // O código HTML cru que você vai editar
  content?: ReactNode; // Conteúdo React complexo (opcional)
  marginNotes?: MarginNoteData[];
}