import React from 'react';
import { Sparkles, UploadCloud, Layers, Library, Search } from 'lucide-react';

const Features: React.FC = () => {
  return (
    <section className="py-20 border-y-2 border-black bg-gray-50 bg-[radial-gradient(#000000_1px,transparent_1px)] [background-size:20px_20px]" id="metodologia">
      <div className="container mx-auto px-6">
        
        <div className="text-center mb-16 max-w-3xl mx-auto bg-white border-2 border-black p-8 shadow-[8px_8px_0px_0px_#000]">
           <div className="flex items-center justify-center gap-2 mb-4">
             <Sparkles className="w-5 h-5 text-blue-600 fill-current" />
             <span className="font-black text-blue-600 uppercase tracking-widest text-sm">Powered by Gemini</span>
           </div>
           <h2 className="font-serif-display italic text-4xl md:text-5xl mb-4 text-black">
            ECOSSISTEMA <span className="bg-yellow-400 px-2 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] text-black not-italic font-sans">INTELIGENTE</span>
           </h2>
           <p className="text-lg text-black font-medium">
             Mais do que um banco de questões. Somos a nuvem de dados que conecta o conteúdo ao seu objetivo.
           </p>
        </div>

        {/* Bento Grid Layout - Brutalist Edition */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          
          {/* Feature 1: The Repository */}
          <div className="md:col-span-2 bg-white border-2 border-black p-8 shadow-[8px_8px_0px_0px_#000] hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[4px_4px_0px_0px_#000] transition-all rounded-none group">
            <div className="bg-blue-600 border-2 border-black w-12 h-12 flex items-center justify-center mb-6 text-white group-hover:bg-blue-500 transition-colors">
              <Library size={24} />
            </div>
            <h3 className="text-2xl font-black mb-2 uppercase">Acervo Comunitário</h3>
            <p className="text-black font-medium leading-relaxed">
              A Netflix dos estudos. Acesse milhares de PDFs, listas de exercícios e mapas mentais compartilhados por outros estudantes. Encontrou um material bom no Telegram? Suba para a Darcy e deixe a IA organizar.
            </p>
          </div>

          {/* Feature 2: Gemini Integration */}
          <div className="md:col-span-2 bg-black text-white border-2 border-black p-8 shadow-[8px_8px_0px_0px_rgba(250,204,21,1)] rounded-none relative overflow-hidden">
            <div className="absolute top-0 right-0 p-32 bg-blue-600 blur-[80px] opacity-40"></div>
            <div className="relative z-10">
              <div className="flex items-center gap-2 mb-6">
                 <Sparkles className="text-yellow-400 fill-current" />
                 <span className="font-mono text-xs text-yellow-400 uppercase border border-yellow-400 px-2 py-0.5">Tecnologia Exclusiva</span>
              </div>
              <h3 className="text-2xl font-black mb-2 uppercase">Gemini: O Analista</h3>
              <p className="text-gray-300 font-medium">
                Não sabe se aquele PDF de 100 páginas vale a pena? O Gemini lê para você, resume os pontos chaves e cruza com o edital do PAS. Economize horas de leitura inútil.
              </p>
            </div>
          </div>

          {/* Feature 3: Search */}
          <div className="md:col-span-1 bg-yellow-400 border-2 border-black p-6 rounded-none hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none shadow-[8px_8px_0px_0px_#000] transition-all">
             <Search className="mb-4 text-black" size={28} />
             <h3 className="font-black text-lg mb-2 uppercase">Busca Unificada</h3>
             <p className="text-sm text-black font-bold">
               Pesquise "Barroco" e encontre aulas, PDFs e questões de todas as fontes em uma tela só.
             </p>
          </div>

          {/* Feature 4: Upload */}
          <div className="md:col-span-1 bg-white border-2 border-black p-6 rounded-none hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none shadow-[8px_8px_0px_0px_#000] transition-all">
             <UploadCloud className="mb-4 text-purple-600" size={28} />
             <h3 className="font-black text-lg mb-2 uppercase">Upload Inteligente</h3>
             <p className="text-sm text-black font-medium">
               Colabore. Ao subir um arquivo, você ganha pontos na comunidade e ajuda a democratizar o acesso.
             </p>
          </div>

          {/* Feature 5: Playlist */}
          <div className="md:col-span-2 bg-white border-2 border-black p-6 rounded-none flex items-center gap-6 hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-none shadow-[8px_8px_0px_0px_#000] transition-all">
             <div className="bg-green-100 border-2 border-black p-4 text-green-700">
                <Layers size={24} />
             </div>
             <div>
               <h3 className="font-black text-lg mb-1 uppercase">Playlists de Curadoria</h3>
               <p className="text-sm text-black font-medium">
                 Siga as playlists de estudos dos aprovados em Medicina. Veja exatamente quais materiais eles usaram.
               </p>
             </div>
          </div>

        </div>
      </div>
    </section>
  );
};

export default Features;