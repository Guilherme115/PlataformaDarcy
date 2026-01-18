import React from 'react';
import { AlertTriangle, Link, Info, Lightbulb } from 'lucide-react';
import { MarginNoteData } from '../types';

// --- Crop Marks ---
export const CropMarks: React.FC = () => (
  <>
    <div className="absolute top-0 left-0 w-[30px] h-[30px] border-r-2 border-b-2 border-black" />
    <div className="absolute top-0 right-0 w-[30px] h-[30px] border-l-2 border-b-2 border-black" />
    <div className="absolute bottom-0 left-0 w-[30px] h-[30px] border-r-2 border-t-2 border-black" />
    <div className="absolute bottom-0 right-0 w-[30px] h-[30px] border-l-2 border-t-2 border-black" />
  </>
);

// --- Highlight Box (The DNA Box) ---
interface HighlightBoxProps {
  label: string;
  children: React.ReactNode;
}

export const HighlightBox: React.FC<HighlightBoxProps> = ({ label, children }) => (
  <div className="relative border-[3px] border-black bg-white p-6 my-8 shadow-brutal">
    <div className="absolute -top-3 left-0 bg-black text-white px-3 py-1 text-[10px] font-mono uppercase font-bold border-2 border-white shadow-sm">
      {label}
    </div>
    {children}
  </div>
);

// --- Margin Note Component ---
export const MarginNote: React.FC<{ data: MarginNoteData }> = ({ data }) => {
  const getIcon = () => {
    switch (data.type) {
      case 'warning': return <AlertTriangle className="w-3 h-3 inline mr-1" />;
      case 'link': return <Link className="w-3 h-3 inline mr-1" />;
      case 'tip': return <Lightbulb className="w-3 h-3 inline mr-1" />;
      default: return <Info className="w-3 h-3 inline mr-1" />;
    }
  };

  const getLabelStyle = () => {
    switch (data.type) {
      case 'warning': return 'bg-darcy-yellow border border-black';
      case 'link': return 'border-b-2 border-black w-full pb-1';
      case 'tip': return 'bg-black text-white px-1';
      default: return 'text-gray-600 border-b border-gray-400';
    }
  };

  return (
    <div className="font-mono text-[8.5pt] text-gray-800 border-t-[3px] border-black pt-2 mb-6 leading-tight">
      {data.title && (
        <strong className={`block uppercase mb-2 w-fit text-[9px] ${getLabelStyle()}`}>
          {getIcon()}
          {data.title}
        </strong>
      )}
      {data.content}
    </div>
  );
};

// --- Graph Placeholder ---
export const GraphExample: React.FC<{ type: 'increasing' | 'decreasing' }> = ({ type }) => {
  const isIncreasing = type === 'increasing';
  
  return (
    <div className="text-center w-full">
      <div className="border-2 border-black bg-gray-50 mb-2 p-2 aspect-square relative">
        <svg width="100%" height="100%" viewBox="0 0 100 100" className="overflow-visible">
          {/* Grid */}
          <line x1="50" y1="0" x2="50" y2="100" className="stroke-gray-300 stroke-[1px]" />
          <line x1="0" y1="50" x2="100" y2="50" className="stroke-gray-300 stroke-[1px]" />
          {/* Axis */}
          <line x1="50" y1="5" x2="50" y2="95" className="stroke-black stroke-[2px]" />
          <line x1="5" y1="50" x2="95" y2="50" className="stroke-black stroke-[2px]" />
          {/* Line */}
          {isIncreasing ? (
            <line x1="20" y1="80" x2="80" y2="20" className="stroke-black stroke-[4px] stroke-linecap-round" />
          ) : (
            <line x1="20" y1="20" x2="80" y2="80" className="stroke-black stroke-[4px] stroke-linecap-round" />
          )}
        </svg>
      </div>
      <span className="font-mono text-[10px] font-black block bg-black text-white py-1 uppercase">
        {isIncreasing ? 'a > 0 (Crescente)' : 'a < 0 (Decrescente)'}
      </span>
    </div>
  );
};

export const NoteArea: React.FC = () => (
    <div className="flex-1 border-l-4 border-dotted border-gray-400 pl-2 relative min-h-[150px] mt-4">
        <span className="text-[9px] font-mono text-gray-400 rotate-90 origin-top-left absolute top-0 left-4 uppercase font-bold tracking-widest whitespace-nowrap">
            Área de Anotações Táticas
        </span>
    </div>
)
