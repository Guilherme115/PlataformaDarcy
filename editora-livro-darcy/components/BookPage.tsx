import React from 'react';
import { PageContent } from '../types';
import { CropMarks, NoteArea, MarginNote } from './PageElements';

interface BookPageProps {
  data: PageContent;
  scale?: number;
}

export const BookPage: React.FC<BookPageProps> = ({ data, scale = 1 }) => {
  const { layout, module, title, pageNumber, htmlContent, content, marginNotes } = data;
  
  const isStandard = layout === 'standard';
  const isCover = layout === 'cover';
  
  // Grid configuration
  const gridStyle = isStandard ? '1fr 60mm' : '1fr';

  return (
    <div 
      className={`bg-paper relative shadow-2xl overflow-hidden mx-auto transition-transform duration-300 origin-top text-ink`}
      style={{
        width: '210mm',
        height: '297mm',
        transform: `scale(${scale})`,
        padding: isCover ? '15mm' : '25mm 20mm 20mm 20mm',
        display: isCover ? 'block' : 'grid',
        gridTemplateColumns: gridStyle,
        gap: '10mm',
        boxShadow: isCover ? '0 0 60px rgba(0,0,0,0.6)' : '0 0 50px rgba(0,0,0,0.5)'
      }}
    >
      <CropMarks />

      {/* --- RENDERIZAÇÃO DE CONTEÚDO --- */}
      
      {isCover ? (
        // LAYOUT CAPA: Renderiza o HTML cru direto
        <div className="h-full w-full border-[6px] border-black p-8 relative">
             <div className="absolute top-0 left-0 w-4 h-4 bg-black" />
             <div className="absolute top-0 right-0 w-4 h-4 bg-black" />
             <div className="absolute bottom-0 left-0 w-4 h-4 bg-black" />
             <div className="absolute bottom-0 right-0 w-4 h-4 bg-black" />
             
             {/* INJEÇÃO DE HTML DO USUÁRIO */}
             {htmlContent ? (
                 <div className="h-full flex flex-col justify-between" dangerouslySetInnerHTML={{ __html: htmlContent }} />
             ) : (
                 <div className="h-full flex flex-col justify-between">
                     {content}
                 </div>
             )}
        </div>
      ) : (
        <>
          {/* LAYOUT PADRÃO & TOC */}
          <div className="flex flex-col h-full">
            
            {/* Cabeçalho Automático (Para páginas padrão) */}
            {layout === 'standard' && (
                <div className="mb-8 border-b-4 border-black pb-4">
                    <span className="font-mono text-xs font-black uppercase tracking-widest block mb-2 bg-black text-white w-fit px-2 py-1">
                        {module}
                    </span>
                    <h1 className="text-4xl leading-none mt-2 font-sans font-black uppercase tracking-tight">
                        {title}
                    </h1>
                </div>
            )}

             {/* Cabeçalho Automático (Para Sumário) */}
             {layout === 'toc' && (
                 <div className="mb-12 border-b-[6px] border-black pb-6 text-center">
                    <h1 className="text-6xl font-sans font-black uppercase tracking-widest">
                        Índice
                    </h1>
                 </div>
            )}

            {/* CONTEÚDO HTML DO USUÁRIO */}
            {htmlContent ? (
                <div className="flex-1" dangerouslySetInnerHTML={{ __html: htmlContent }} />
            ) : (
                <div className="flex-1">
                    {content}
                </div>
            )}
          </div>

          {/* Coluna Lateral (Apenas Padrão) */}
          {isStandard && (
            <div className="flex flex-col pt-24 h-full">
              
              {marginNotes ? (
                  marginNotes.map(note => <MarginNote key={note.id} data={note} />)
              ) : (
                <div className="font-mono text-[8.5pt] text-gray-800 border-t-[3px] border-black pt-2 mb-6 leading-tight">
                    <strong className="block uppercase mb-2 w-fit text-[9px] bg-darcy-yellow border border-black px-1">
                        Nota do Editor
                    </strong>
                    Esta área lateral é fixa no layout padrão. Edite o conteúdo principal à esquerda.
                </div>
              )}
              
              <NoteArea />
            </div>
          )}
        </>
      )}

      {/* Rodapé Automático */}
      {!isCover && (
        <div className="absolute bottom-[15mm] left-[20mm] right-[20mm] border-t-2 border-black pt-2 flex justify-between font-mono text-[9pt] font-bold">
          <span className="uppercase tracking-widest text-black">
             Plataforma Darcy • {module}
          </span>
          <span className="font-black bg-black text-white px-2">
            PÁGINA {pageNumber.toString().padStart(2, '0')}
          </span>
        </div>
      )}
    </div>
  );
};