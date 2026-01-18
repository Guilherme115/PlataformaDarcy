import React from 'react';

const Ticker: React.FC = () => {
  const items = [
    "PAS 1", "•", "CEBRASPE", "•", "15 ANOS DE DADOS", "•", "+5.000 QUESTÕES", "•",
    "IA TREINADA", "•", "PODCAST EDUCATIVO", "•", "ALGORITMO ADAPTATIVO", "•",
    "CADERNO DE ERROS", "•", "PAS 2", "•", "PAS 3", "•", "UNB"
  ];

  return (
    <div className="bg-black text-yellow-400 py-3 border-b-4 border-black overflow-hidden whitespace-nowrap select-none">
      <div className="inline-block animate-marquee">
        {[...items, ...items, ...items, ...items].map((item, index) => (
          <span key={index} className="text-sm md:text-base font-bold font-mono uppercase tracking-widest mx-4">
            {item}
          </span>
        ))}
      </div>
    </div>
  );
};

export default Ticker;