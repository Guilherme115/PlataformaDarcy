import React from 'react';
import { ArrowRight } from 'lucide-react';

const Footer: React.FC = () => {
  return (
    <footer className="bg-black text-white pt-24 pb-8 border-t-8 border-yellow-400">
      <div className="container mx-auto px-6">
        
        {/* Main CTA */}
        <div className="text-center mb-24">
          <h2 className="font-serif-display italic text-5xl md:text-7xl text-yellow-400 mb-6">
            A UNIVERSIDADE<br/>É NOSSA.
          </h2>
          <p className="text-gray-400 max-w-lg mx-auto mb-8">
            Darcy Ribeiro sonhou com uma universidade para o povo. Nós estamos construindo a ferramenta que vai te levar até lá.
          </p>
          <button className="bg-yellow-400 text-black px-10 py-5 text-xl font-bold inline-flex items-center gap-3 hover:bg-white hover:scale-105 transition-all shadow-[0px_0px_20px_rgba(250,204,21,0.4)]">
            OCUPAR MINHA VAGA AGORA <ArrowRight />
          </button>
        </div>

        <hr className="border-gray-800 mb-12" />

        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 text-sm">
          <div>
            <div className="w-10 h-10 bg-white text-black font-serif-display italic flex items-center justify-center text-xl font-bold mb-6">
              D.
            </div>
            <p className="text-gray-500">
              Brasília - DF<br/>
              Feito por candangos.
            </p>
          </div>
          
          <div>
            <h4 className="font-bold text-yellow-400 uppercase tracking-widest mb-4">Plataforma</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="#" className="hover:text-white">Simulados</a></li>
              <li><a href="#" className="hover:text-white">Tutor IA</a></li>
              <li><a href="#" className="hover:text-white">Acervo Wiki</a></li>
              <li><a href="#" className="hover:text-white">Caderno de Erros</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-yellow-400 uppercase tracking-widest mb-4">Comunidade</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="#" className="hover:text-white">Manifesto</a></li>
              <li><a href="#" className="hover:text-white">Código Aberto</a></li>
              <li><a href="#" className="hover:text-white">Contribuir</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-yellow-400 uppercase tracking-widest mb-4">Contato</h4>
            <ul className="space-y-2 text-gray-400">
              <li><a href="#" className="hover:text-white">contato@plataformadarcy.com.br</a></li>
              <li><a href="#" className="hover:text-white">WhatsApp Suporte</a></li>
            </ul>
          </div>
        </div>

        <div className="mt-16 text-center text-xs text-gray-600 uppercase tracking-widest">
          Plataforma Darcy © 2024 - Todos os direitos reservados à revolução.
        </div>
      </div>
    </footer>
  );
};

export default Footer;