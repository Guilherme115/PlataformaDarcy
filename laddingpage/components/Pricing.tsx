import React, { useState } from 'react';
import { Check, X, ChevronDown, HelpCircle, Zap, Coffee, Trophy } from 'lucide-react';
import { PricingTier } from '../types';

const FAQItem = ({ question, answer }: { question: string, answer: string }) => {
  const [isOpen, setIsOpen] = useState(false);
  return (
      <div className="border-2 border-black bg-white mb-4 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:translate-x-[1px] hover:translate-y-[1px] hover:shadow-[3px_3px_0px_0px_rgba(0,0,0,1)]">
        <button
            className="w-full text-left p-4 font-black flex justify-between items-center hover:bg-yellow-400 transition-colors"
            onClick={() => setIsOpen(!isOpen)}
        >
          <span className="uppercase text-sm md:text-base">{question}</span>
          <ChevronDown className={`transform transition-transform ${isOpen ? 'rotate-180' : ''}`} />
        </button>
        {isOpen && (
            <div className="p-4 border-t-2 border-black text-black font-medium text-sm leading-relaxed bg-gray-50">
              {answer}
            </div>
        )}
      </div>
  );
};

const Pricing: React.FC = () => {
  const plans: PricingTier[] = [
    {
      name: "OBSERVADOR",
      price: "R$ 0",
      period: "/mês",
      features: ["Download de Provas Antigas", "Acesso à Comunidade", "Upload de Materiais", "Visualizar Aulas Compartilhadas"],
      notIncluded: ["Resumo Automático (IA)", "Podcast Gerado (IA)", "Correção de Redação (IA)"],
      cta: "CRIAR CONTA GRÁTIS",
      highlight: false
    },
    {
      name: "OPERADOR DE DADOS",
      price: "R$ 29,90",
      period: "/mês",
      features: [
        "IA Gemini Ilimitada",
        "Busca Sniper (Conteúdo Interno)",
        "Podcast IA Diário",
        "Prioridade de Suporte",
        "Apoia o Desenvolvedor ❤️"
      ],
      cta: "ASSINAR E APOIAR",
      highlight: true
    }
  ];

  return (
      <section className="py-24 bg-white bg-grid border-y-4 border-black relative overflow-hidden" id="planos">

        <div className="container mx-auto px-6 relative z-10">
          <div className="text-center mb-12">
            <div className="inline-block bg-red-600 text-white font-bold px-4 py-1 mb-4 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] uppercase animate-pulse">
              Preço de Lançamento
            </div>
            <h2 className="font-serif-display italic text-6xl mb-4 text-black">ACESSO AO SISTEMA</h2>
            <p className="text-black font-mono text-xs uppercase tracking-widest font-bold">
              Transparência total sobre custos e sustentabilidade.
            </p>
          </div>

          {/* Developer Transparency Note */}
          <div className="max-w-4xl mx-auto mb-24 md:mb-16 relative">

            <div className="bg-gray-50 border-4 border-black p-6 md:p-8 flex flex-col md:flex-row gap-6 items-start shadow-[12px_12px_0px_0px_#000] relative z-10">
              <div className="bg-blue-600 border-2 border-black p-4 text-white flex-shrink-0">
                <Coffee size={32} />
              </div>
              <div className="md:pr-24">
                <h3 className="font-black text-xl mb-3 flex items-center gap-2 uppercase bg-black text-white px-2 inline-block transform -rotate-1">
                  Papo reto de Desenvolvedor
                </h3>
                <div className="space-y-3 text-black font-medium leading-relaxed text-sm md:text-base border-l-4 border-black pl-4 mt-2">
                  <p>
                    Olá! Aqui quem fala é o dev. A lógica da <strong>Darcy</strong> é simples:
                  </p>
                  <ul className="list-disc pl-5 space-y-1 marker:text-black">
                    <li>
                      <strong>O que é estático é grátis:</strong> Baixar provas e ver aulas não custa nada para a plataforma. O conhecimento deve circular livremente.
                    </li>
                    <li>
                      <strong>O que processa dados é pago:</strong> A IA do Gemini gera custos de API a cada clique.
                    </li>
                  </ul>
                  <p className="font-bold text-black pt-2 bg-yellow-200 inline-block px-1 border border-black">
                    A assinatura serve estritamente para manter o site no ar e pagar a API do Google.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-col lg:flex-row justify-center items-stretch gap-8 max-w-5xl mx-auto mb-20">
            {plans.map((plan) => (
                <div
                    key={plan.name}
                    className={`w-full max-w-md p-8 border-4 border-black relative flex flex-col transition-all duration-300 ${
                        plan.highlight
                            ? 'bg-white shadow-[16px_16px_0px_0px_#000] scale-100 lg:scale-105 z-10'
                            : 'bg-white opacity-100 shadow-[8px_8px_0px_0px_#000]'
                    }`}
                >
                  {plan.highlight && (
                      <div className="absolute -top-6 left-1/2 -translate-x-1/2 bg-black text-yellow-400 text-sm font-black px-6 py-2 border-2 border-yellow-400 shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] uppercase tracking-wider whitespace-nowrap flex items-center gap-2">
                        <Zap size={16} /> Mais Escolhido
                      </div>
                  )}

                  <h3 className="text-2xl font-black uppercase mb-2 bg-black text-white inline-block px-2">{plan.name}</h3>
                  <div className="flex items-baseline mb-8 pb-8 border-b-4 border-black mt-4">
                <span className={`text-5xl font-serif-display italic text-black`}>
                  {plan.price}
                </span>
                    <span className="text-sm font-bold text-gray-500 ml-2">{plan.period}</span>
                  </div>

                  <ul className="space-y-4 mb-8 flex-grow">
                    {plan.features.map((feature) => (
                        <li key={feature} className="flex items-start gap-3 text-sm font-bold text-black">
                          <div className={`mt-0.5 p-0.5 border border-black ${plan.highlight ? 'bg-yellow-400 text-black' : 'bg-gray-200 text-black'}`}>
                            <Check className="w-3 h-3" />
                          </div>
                          {feature}
                        </li>
                    ))}
                    {plan.notIncluded?.map((feature) => (
                        <li key={feature} className="flex items-start gap-3 text-sm text-gray-400 line-through decoration-2 decoration-red-500">
                          <X className="w-4 h-4 text-red-500" />
                          {feature}
                        </li>
                    ))}
                  </ul>

                  <button
                      className={`w-full py-5 font-black text-lg uppercase tracking-wide border-2 border-black transition-all shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] ${
                          plan.highlight
                              ? 'bg-blue-600 text-white hover:bg-blue-700'
                              : 'bg-white text-black hover:bg-gray-100'
                      }`}
                  >
                    {plan.cta}
                  </button>
                </div>
            ))}
          </div>

          {/* FAQ Section */}
          <div className="max-w-3xl mx-auto mb-20">
            <div className="flex items-center gap-2 mb-8 justify-center">
              <HelpCircle className="w-8 h-8 text-black" />
              <h3 className="text-3xl font-serif-display italic text-black">Protocolo de Dúvidas</h3>
            </div>

            <FAQItem
                question="Posso usar de graça para sempre?"
                answer="Sim! Se você só quer baixar provas antigas, ver as aulas que a galera postou e participar dos fóruns, você nunca vai pagar nada. A conta 'Observador' é vitalícia."
            />
            <FAQItem
                question="Por que a IA é paga?"
                answer="Toda vez que você pede para a IA resumir um PDF ou criar um podcast, a Darcy paga uma taxa para o Google (API). Para o projeto não quebrar, precisamos repassar esse custo de processamento apenas para quem usa esses recursos avançados."
            />
            <FAQItem
                question="O que é a Busca Sniper?"
                answer="É um motor de busca interno que indexa milhares de PDFs de domínio público e materiais compartilhados pela comunidade. Você digita 'Barroco' e a IA te entrega os parágrafos exatos que importam."
            />
          </div>

          {/* FUN CARD - HEXA */}
          <div className="flex justify-center pb-8">
            <div className="relative group hover:-rotate-1 transition-transform duration-300">
              {/* Tape effect */}
              <div className="absolute -top-3 left-1/2 -translate-x-1/2 w-24 h-6 bg-yellow-200/80 rotate-3 border-l border-r border-white/50 backdrop-blur-sm z-20 shadow-sm"></div>

              <div className="bg-white p-4 pb-6 border-4 border-black shadow-[8px_8px_0px_0px_#166534] rotate-2 cursor-help transition-all hover:scale-105">

                {/* Image Container with precise aspect ratio 407/643 */}
                <div
                    className="bg-gray-100 border-2 border-black mb-4 relative grayscale group-hover:grayscale-0 transition-all duration-500 overflow-hidden"
                    style={{ width: '300px', aspectRatio: '407/643' }}
                >
                  <img
                      src="/darcy-neymar.png"
                      alt="Darcy Ribeiro com a camisa do Brasil ao lado do Neymar"
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.style.display = 'none';
                        target.parentElement!.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-center p-2"><span class="text-2xl mb-2">⚽</span><span class="font-bold text-xs">SALVAR COMO:</span><span class="font-mono text-[10px] bg-yellow-200 px-1 mt-1">/public/darcy-neymar.png</span></div>';
                      }}
                  />
                  {/* Grain overlay */}
                  <div className="absolute inset-0 bg-black/5 pointer-events-none"></div>
                </div>

                <div className="flex justify-center -mt-10 mb-2 relative z-10">
                  <div className="bg-yellow-400 border-2 border-black rounded-full p-3 shadow-md">
                    <Trophy size={24} className="text-black" />
                  </div>
                </div>

                <h3 className="font-serif-display italic text-2xl text-center leading-tight max-w-[300px]">
                  "O dono deste site acredita no <span className="text-green-600 font-black decoration-wavy underline">Hexa</span>."
                </h3>
                <div className="mt-2 text-center">
                   <span className="text-[10px] font-mono font-bold text-gray-400 uppercase">
                     (Prioridades são prioridades)
                   </span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </section>
  );
};

export default Pricing;