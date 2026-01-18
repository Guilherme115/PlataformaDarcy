import React from 'react';

const Stats: React.FC = () => {
  const stats = [
    { value: "12TB+", label: "Dados Processados", sub: "Pelo Gemini" },
    { value: "8.5k", label: "Arquivos no Acervo", sub: "PDFs e Vídeos" },
    { value: "15+", label: "Anos de Provas", sub: "Indexados" },
    { value: "24/7", label: "Inteligência", sub: "Ativa" },
  ];

  return (
    <section className="bg-white border-b-4 border-black" id="ia">
      <div className="grid grid-cols-2 md:grid-cols-4">
        {stats.map((stat, idx) => (
          <div 
            key={idx} 
            className="group relative border-r-2 border-black last:border-r-0 border-b-2 md:border-b-0 p-8 flex flex-col items-center justify-center text-center bg-white hover:bg-yellow-400 transition-colors duration-0"
          >
            {/* Corner Accent */}
            <div className="absolute top-2 right-2 w-2 h-2 bg-black opacity-0 group-hover:opacity-100 transition-opacity"></div>
            <div className="absolute bottom-2 left-2 w-2 h-2 bg-black opacity-0 group-hover:opacity-100 transition-opacity"></div>

            <span className="font-serif-display italic text-4xl md:text-6xl font-black mb-2 text-black drop-shadow-[2px_2px_0px_rgba(255,255,255,1)] group-hover:drop-shadow-[2px_2px_0px_rgba(255,255,255,0.5)]">
              {stat.value}
            </span>
            <span className="font-mono text-xs font-black uppercase tracking-widest text-blue-600 bg-blue-100 px-2 py-0.5 border border-black transform -rotate-2 group-hover:rotate-0 transition-transform">
              {stat.label}
            </span>
            <span className="text-[10px] font-bold text-gray-500 uppercase mt-2 group-hover:text-black">
              {stat.sub}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
};

export default Stats;