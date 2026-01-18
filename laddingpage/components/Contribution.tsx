import React from 'react';
import { UploadCloud, Share2, ShieldCheck, Cpu } from 'lucide-react';

const Contribution: React.FC = () => {
  return (
    <section className="bg-yellow-400 py-24 border-y-4 border-black relative overflow-hidden">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-10 pointer-events-none" 
           style={{ backgroundImage: 'radial-gradient(circle, #000 1px, transparent 1px)', backgroundSize: '20px 20px' }}>
      </div>

      <div className="container mx-auto px-6 relative z-10">
        <div className="flex flex-col md:flex-row items-center gap-16">
          
          {/* Text Content */}
          <div className="md:w-1/2">
            <div className="inline-block bg-black text-white px-4 py-1 font-mono text-sm font-bold uppercase mb-6 transform -rotate-1">
              Crowdsourcing de Dados
            </div>
            
            <h2 className="font-serif-display italic text-5xl md:text-6xl text-black mb-6 leading-none">
              O CONHECIMENTO<br/>
              NÃO PODE TER<br/>
              <span className="text-white drop-shadow-[4px_4px_0px_rgba(0,0,0,1)]">DONO.</span>
            </h2>
            
            <div className="space-y-6 text-lg font-medium border-l-4 border-black pl-6">
              <p>
                A <strong>Darcy</strong> não é apenas uma plataforma, é um organismo vivo. Nossa IA precisa de dados para ficar mais inteligente.
              </p>
              <p>
                Tem um resumo incrível no seu caderno? Uma lista de exercícios do seu cursinho presencial? <br/>
                <strong>Compartilhe.</strong>
              </p>
              <p className="text-sm font-mono bg-white/50 p-2 border border-black inline-block">
                Ao fazer upload, você ajuda milhares de estudantes que não têm acesso a material de qualidade.
              </p>
            </div>

            <button className="mt-8 bg-black text-white px-8 py-4 text-xl font-bold flex items-center gap-3 hover:bg-white hover:text-black hover:scale-105 transition-all shadow-[8px_8px_0px_0px_#fff]">
              <UploadCloud className="w-6 h-6" />
              ENVIAR MATERIAL AGORA
            </button>
          </div>

          {/* Visual Representation */}
          <div className="md:w-1/2 w-full">
            <div className="bg-white border-4 border-black p-8 shadow-[16px_16px_0px_0px_rgba(0,0,0,1)] relative">
              
              <div className="absolute -top-6 -right-6 bg-blue-600 text-white font-bold p-4 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] rotate-6 z-20">
                <div className="text-xs uppercase">Recompensa</div>
                <div className="text-xl">Acesso Premium</div>
              </div>

              <h3 className="font-black text-2xl uppercase mb-8 border-b-2 border-dashed border-gray-300 pb-4">
                Ciclo de Dados
              </h3>

              <div className="space-y-6">
                {/* Step 1 */}
                <div className="flex items-center gap-4 group">
                  <div className="w-12 h-12 bg-gray-100 border-2 border-black flex items-center justify-center group-hover:bg-yellow-400 transition-colors">
                    <Share2 className="w-6 h-6" />
                  </div>
                  <div>
                    <h4 className="font-bold uppercase text-sm">1. Você envia</h4>
                    <p className="text-xs text-gray-500">PDFs, Imagens, Resumos</p>
                  </div>
                </div>

                {/* Connector */}
                <div className="h-6 w-0 border-l-2 border-dashed border-black ml-6"></div>

                {/* Step 2 */}
                <div className="flex items-center gap-4 group">
                  <div className="w-12 h-12 bg-gray-100 border-2 border-black flex items-center justify-center group-hover:bg-blue-500 group-hover:text-white transition-colors">
                    <Cpu className="w-6 h-6" />
                  </div>
                  <div>
                    <h4 className="font-bold uppercase text-sm">2. Gemini Processa</h4>
                    <p className="text-xs text-gray-500">Categorização e Resumo Automático</p>
                  </div>
                </div>

                {/* Connector */}
                <div className="h-6 w-0 border-l-2 border-dashed border-black ml-6"></div>

                {/* Step 3 */}
                <div className="flex items-center gap-4 group">
                  <div className="w-12 h-12 bg-gray-100 border-2 border-black flex items-center justify-center group-hover:bg-green-500 group-hover:text-white transition-colors">
                    <ShieldCheck className="w-6 h-6" />
                  </div>
                  <div>
                    <h4 className="font-bold uppercase text-sm">3. Comunidade Recebe</h4>
                    <p className="text-xs text-gray-500">Material Gratuito e Indexado</p>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Contribution;