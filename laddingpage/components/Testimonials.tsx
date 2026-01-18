import React from 'react';
import { Quote, CheckCircle2 } from 'lucide-react';

const Testimonials: React.FC = () => {
  const testimonials = [
    {
      text: "Eu achava que sabia estudar, mas o Caderno de Erros me mostrou que eu só revisava o que já sabia. Passei em Direito na UnB no PAS 3.",
      author: "Ana Beatriz",
      course: "Direito - UnB",
      year: "Aprovada 2023",
      avatarColor: "bg-purple-500"
    },
    {
      text: "A IA explicando as obras literárias salvou minha vida. Eu não tinha tempo de ler tudo, os resumos e as questões focadas foram cirúrgicos.",
      author: "João Pedro",
      course: "Eng. Software - UnB",
      year: "Aprovado 2024",
      avatarColor: "bg-blue-500"
    },
    {
      text: "Paguei 19 reais e economizei 1 ano de cursinho. A análise de dados do que cai no PAS 1 é bizarra de precisa.",
      author: "Mariana S.",
      course: "Medicina - UnB",
      year: "Aprovada 2023",
      avatarColor: "bg-green-500"
    }
  ];

  return (
    <section className="py-24 bg-gray-50 bg-[radial-gradient(#000000_1px,transparent_1px)] [background-size:16px_16px]">
      <div className="container mx-auto px-6">
        <div className="text-center mb-16">
          <p className="font-mono text-xs font-bold uppercase tracking-[0.3em] text-red-600 mb-4 bg-white inline-block px-2 border border-black">HALL DA FAMA</p>
          <h2 className="font-serif-display italic text-4xl md:text-6xl max-w-2xl mx-auto">
            QUEM SEGUE O MÉTODO <span className="bg-yellow-400 px-2">OCUPA A VAGA.</span>
          </h2>
        </div>

        <div className="grid md:grid-cols-3 gap-8">
          {testimonials.map((t, i) => (
            <div 
              key={i} 
              className="bg-white p-8 border-4 border-black relative flex flex-col justify-between h-full shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] hover:scale-[1.02] transition-transform"
            >
              {/* Pin Decoration */}
              <div className="absolute -top-4 left-1/2 -translate-x-1/2 w-4 h-4 rounded-full bg-red-500 border-2 border-black z-10 shadow-sm"></div>
              
              <Quote className="w-10 h-10 mb-6 text-gray-200 fill-black absolute top-4 right-4" />
              
              <p className="text-lg font-medium leading-relaxed mb-8 relative z-10 font-grotesk">"{t.text}"</p>
              
              <div className="flex items-center gap-4 mt-auto pt-6 border-t-2 border-dashed border-gray-300">
                <div className={`w-12 h-12 flex items-center justify-center font-bold text-xl text-white border-2 border-black shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] ${t.avatarColor}`}>
                  {t.author.charAt(0)}
                </div>
                <div>
                  <p className="font-bold uppercase text-sm flex items-center gap-1">
                    {t.author} <CheckCircle2 className="w-4 h-4 text-blue-500" />
                  </p>
                  <p className="text-xs font-bold text-gray-500">{t.course}</p>
                  <span className="inline-block mt-1 bg-green-100 text-green-800 text-[10px] font-bold px-2 py-0.5 border border-green-800 rounded-full">
                    {t.year}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        {/* Call to Action Mini */}
        <div className="mt-16 text-center">
            <p className="font-bold text-xl mb-4">Você é o próximo?</p>
            <button className="text-sm font-mono uppercase underline hover:bg-black hover:text-white px-2 py-1 transition-colors">
                Ver lista completa de aprovados
            </button>
        </div>
      </div>
    </section>
  );
};

export default Testimonials;