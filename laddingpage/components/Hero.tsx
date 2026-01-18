import React from 'react';
import { ArrowRight, Database, Share2, FileText, PlayCircle, Sparkles } from 'lucide-react';

const Hero: React.FC = () => {
  return (
    <section className="relative pt-32 pb-16 md:pt-40 md:pb-24 border-b-4 border-black bg-grid overflow-hidden">
      
      <div className="container mx-auto px-6 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          
          {/* Text Content */}
          <div className="lg:col-span-7 flex flex-col gap-6">
            
            {/* Gemini Badge - High Credibility */}
            <div className="inline-flex items-center gap-3 border-2 border-black bg-white px-5 py-3 w-fit shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rounded-lg hover:translate-y-[-2px] transition-transform cursor-default">
               {/* Abstract Google Gemini Colors Representation */}
               <div className="flex space-x-1">
                 <div className="w-2 h-4 bg-blue-500 rounded-full animate-pulse"></div>
                 <div className="w-2 h-4 bg-red-500 rounded-full animate-pulse delay-75"></div>
                 <div className="w-2 h-4 bg-yellow-400 rounded-full animate-pulse delay-150"></div>
                 <div className="w-2 h-4 bg-green-500 rounded-full animate-pulse delay-200"></div>
               </div>
               <span className="text-sm font-bold tracking-tight text-gray-800 border-l-2 border-gray-200 pl-3">
                 AI Powered by <span className="font-black text-black">Google Gemini™</span>
               </span>
            </div>

            <h1 className="text-5xl md:text-7xl leading-[0.95] font-black tracking-tighter text-gray-900">
              <span className="font-serif-display italic block mb-3 text-gray-500">SOLUÇÃO DE</span>
              <span className="block mb-2">DADOS PARA O</span>
              <span className="block relative inline-block text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-blue-800 drop-shadow-[2px_2px_0px_rgba(0,0,0,1)]">
                VESTIBULAR.
              </span>
            </h1>

            <p className="text-lg md:text-xl text-gray-700 max-w-xl font-medium my-4 leading-relaxed border-l-4 border-blue-600 pl-4 bg-white/60 p-2 backdrop-blur-sm">
              Não somos um cursinho. Somos a infraestrutura que falta no seu estudo. 
              <br/>
              Centralize PDFs externos, videoaulas e use nossa IA para extrair inteligência do caos.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 mt-4">
              <button className="relative bg-black text-white px-8 py-4 text-xl font-bold flex items-center justify-center gap-3 hover:bg-yellow-400 hover:text-black transition-all shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] hover:shadow-[4px_4px_0px_0px_#000] hover:translate-x-1 hover:translate-y-1 group rounded-lg">
                <Database className="w-5 h-5" />
                ACESSAR BANCO DE DADOS
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </button>
              <button className="px-8 py-4 text-lg font-bold border-2 border-black bg-white flex items-center justify-center gap-2 hover:bg-gray-100 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,0.2)] rounded-lg">
                <Share2 className="w-5 h-5" /> Enviar Arquivo
              </button>
            </div>
          </div>

          {/* Visual/Image Area - Data Hub Representation */}
          <div className="lg:col-span-5 relative hidden md:block">
            <div className="relative z-10 group perspective-1000">
              
              {/* Card Main: Content Hub */}
              <div className="border-4 border-black bg-white p-6 shadow-[16px_16px_0px_0px_rgba(0,0,0,1)] rotate-1 group-hover:rotate-0 transition-transform duration-500 rounded-xl relative z-20">
                <div className="flex items-center justify-between border-b-2 border-gray-100 pb-4 mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-3 h-3 rounded-full bg-red-500"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-400"></div>
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                  </div>
                  <span className="text-xs font-bold text-gray-400 uppercase">Dashboard do Aluno</span>
                </div>

                {/* File Item 1 */}
                <div className="flex items-center gap-4 p-3 bg-gray-50 border border-gray-200 rounded-lg mb-3">
                   <div className="p-2 bg-red-100 text-red-600 rounded-md border border-red-200">
                     <PlayCircle size={20} />
                   </div>
                   <div className="flex-1">
                     <h4 className="font-bold text-sm">Aula Ext: Funções (Canal X)</h4>
                     <p className="text-[10px] text-gray-500">Fonte: Youtube Import • 45min</p>
                   </div>
                   <span className="text-xs font-bold text-green-600 bg-green-100 px-2 py-1 rounded">Verificado</span>
                </div>

                {/* File Item 2 */}
                <div className="flex items-center gap-4 p-3 bg-gray-50 border border-gray-200 rounded-lg mb-3">
                   <div className="p-2 bg-blue-100 text-blue-600 rounded-md border border-blue-200">
                     <FileText size={20} />
                   </div>
                   <div className="flex-1">
                     <h4 className="font-bold text-sm">Resumo: Barroco (PAS 1)</h4>
                     <p className="text-[10px] text-gray-500">Colaborador: @ana_b • PDF</p>
                   </div>
                   <span className="text-xs font-bold text-purple-600 bg-purple-100 px-2 py-1 rounded flex gap-1 items-center">
                     <Sparkles size={10} /> IA Resumiu
                   </span>
                </div>

                {/* AI Insight */}
                <div className="mt-4 p-4 bg-blue-50 border-2 border-blue-100 rounded-lg relative overflow-hidden">
                  <div className="absolute top-0 right-0 p-2 opacity-10">
                    <Sparkles size={40} className="text-blue-500"/>
                  </div>
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-xs font-black text-blue-800 uppercase bg-blue-200 px-2 py-0.5 rounded">GEMINI INSIGHT</span>
                  </div>
                  <p className="text-xs text-gray-700 leading-snug font-medium">
                    "Analisei o PDF enviado por @ana_b. O tópico 'Gregório de Matos' tem 85% de chance de cair no PAS 1 este ano."
                  </p>
                </div>
              </div>

              {/* Decorative Background Card */}
              <div className="absolute top-4 -right-4 w-full h-full bg-black rounded-xl -z-10"></div>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
};

export default Hero;