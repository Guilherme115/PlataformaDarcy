import React from 'react';
import { Sparkles, Heart, Quote } from 'lucide-react';

const TechIllustration: React.FC = () => {
    return (
        <section className="py-20 md:py-32 bg-yellow-400 border-y-4 border-black overflow-hidden relative" id="origem">
            {/* Background Texture */}
            <div className="absolute inset-0 opacity-10 pointer-events-none"
                 style={{
                     backgroundImage: 'radial-gradient(#000 2px, transparent 2px)',
                     backgroundSize: '30px 30px'
                 }}>
            </div>

            <div className="container mx-auto px-6 relative z-10">
                <div className="flex flex-col-reverse lg:flex-row items-center gap-16">

                    {/* Left: The Narrative */}
                    <div className="lg:w-1/2 space-y-8">
                        <div className="inline-block bg-white text-black font-black px-6 py-2 border-4 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] uppercase transform -rotate-2">
                            A Gênese do Projeto
                        </div>

                        <h2 className="font-serif-display italic text-5xl md:text-7xl leading-[0.9] text-black drop-shadow-sm">
                            O HUMANISTA <br/>
                            ENCONTRA <br/>
                            <span className="text-white bg-black px-2 decoration-wavy underline decoration-yellow-400">A MÁQUINA.</span>
                        </h2>

                        <div className="prose-lg font-medium border-l-8 border-black pl-6 space-y-4 text-black bg-white/50 p-6 shadow-[8px_8px_0px_0px_rgba(0,0,0,0.1)]">
                            <p>
                                Darcy Ribeiro sempre sonhou com uma universidade aberta, pulsante e acessível a todos os "candangos". Mas ele nunca imaginou que, décadas depois, teríamos uma inteligência artificial capaz de ler milhares de páginas em segundos.
                            </p>
                            <p>
                                <strong>Esta plataforma nasce desse abraço improvável.</strong>
                            </p>
                            <p>
                                Não usamos a IA para substituir o pensamento crítico, mas para limpar o terreno. O Gemini (nosso "polvo" digital) faz o trabalho braçal de organizar os dados, para que você tenha tempo de fazer o que Darcy mais valorizava: <strong>pensar.</strong>
                            </p>
                        </div>

                        <div className="flex items-center gap-4 mt-6">
                            <div className="w-16 h-16 bg-black rounded-full flex items-center justify-center text-yellow-400 border-4 border-white shadow-lg">
                                <Heart fill="currentColor" />
                            </div>
                            <div>
                                <p className="font-bold uppercase text-sm">Feito com paixão em</p>
                                <p className="font-black text-xl">Brasília, DF</p>
                            </div>
                        </div>
                    </div>

                    {/* Right: The Masterpiece Image */}
                    <div className="lg:w-1/2 w-full flex justify-center perspective-1000">
                        <div className="relative w-full max-w-lg group">

                            {/* Decorative Elements behind */}
                            <div className="absolute -top-12 -right-12 text-[120px] text-black opacity-10 font-black z-0 pointer-events-none rotate-12">
                                DF
                            </div>

                            {/* Black Frame Background */}
                            <div className="absolute inset-0 bg-black transform translate-x-6 translate-y-6 rounded-none border-2 border-black"></div>

                            {/* Main Image Container */}
                            <div className="relative bg-white border-4 border-black p-4 pb-16 shadow-2xl transform transition-transform duration-500 hover:-rotate-1 hover:-translate-y-2">

                                {/* The Image - Aspect Ratio Fixed to Square for 1024x1008 */}
                                <div className="border-2 border-black bg-gray-100 overflow-hidden relative aspect-square flex items-center justify-center">
                                    <img
                                        src="/darcy-gemini.png"
                                        alt="Ilustração do Professor Darcy abraçando o Polvo Gemini"
                                        className="object-cover w-full h-full hover:scale-105 transition-transform duration-700"
                                        onError={(e) => {
                                            const target = e.target as HTMLImageElement;
                                            target.style.display = 'none';
                                            target.parentElement!.innerHTML = '<div class="flex flex-col items-center justify-center h-full text-center p-6"><span class="text-4xl mb-4">🖼️</span><span class="font-bold border-2 border-black px-2 bg-yellow-400">IMAGEM AQUI</span><span class="text-xs mt-2 font-mono">/public/darcy-gemini.png</span></div>';
                                        }}
                                    />

                                    {/* Overlay Tag */}
                                    <div className="absolute top-4 left-4 bg-red-600 text-white font-bold px-3 py-1 border-2 border-black shadow-[4px_4px_0px_0px_#000] text-xs uppercase animate-pulse">
                                        Figura Rara
                                    </div>
                                </div>

                                {/* Caption / Handwriting Style */}
                                <div className="absolute bottom-4 left-4 right-4 flex justify-between items-end">
                                    <div className="font-serif-display italic text-2xl text-black">
                                        "O Abraço Digital"
                                    </div>
                                    <div className="font-mono text-xs font-bold text-gray-400">
                                        EST. 2024
                                    </div>
                                </div>

                                {/* Speech Bubble Decoration */}
                                <div className="absolute -top-10 -left-10 md:-left-16 bg-white border-4 border-black px-6 py-4 rounded-[50%] rounded-br-none shadow-[8px_8px_0px_0px_#000] z-20 hidden md:block transform -rotate-12">
                                    <Quote className="w-6 h-6 text-black mb-1 fill-yellow-400" />
                                    <p className="font-black text-sm uppercase leading-tight">O futuro<br/>é nosso!</p>
                                </div>

                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </section>
    );
};

export default TechIllustration;