import React, { useState, useEffect } from 'react';
import { Menu, X, Square } from 'lucide-react';

const Header: React.FC = () => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { name: 'Filosofia', href: '#filosofia' },
    { name: 'IA', href: '#ia' },
    { name: 'Metodologia', href: '#metodologia' },
    { name: 'Planos', href: '#planos', highlight: true },
  ];

  return (
    <header 
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 border-b border-black ${
        isScrolled ? 'bg-white py-3' : 'bg-white py-5'
      }`}
    >
      <div className="container mx-auto px-6 flex justify-between items-center">
        {/* Logo */}
        <div className="flex items-center gap-2 font-black text-2xl tracking-tighter">
          <div className="bg-yellow-400 border-2 border-black w-10 h-10 flex items-center justify-center font-serif-display italic">
            D.
          </div>
          <span>PLATAFORMA DARCY</span>
        </div>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-8 font-mono text-sm font-semibold uppercase tracking-wide">
          {navLinks.map((link) => (
            <a 
              key={link.name} 
              href={link.href}
              className={`hover:text-yellow-600 transition-colors ${link.highlight ? 'text-red-600' : 'text-black'}`}
            >
              {link.name}
            </a>
          ))}
          <div className="h-6 w-px bg-gray-300 mx-2"></div>
          <button className="hover:underline underline-offset-4">Login</button>
          <button className="bg-black text-white px-6 py-2 font-bold hover:bg-yellow-400 hover:text-black hover:scale-105 transition-all duration-200 shadow-[4px_4px_0px_0px_rgba(0,0,0,0.2)]">
            CRIAR CONTA
          </button>
        </nav>

        {/* Mobile Toggle */}
        <button 
          className="md:hidden p-2"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden absolute top-full left-0 right-0 bg-white border-b-2 border-black p-6 flex flex-col gap-4 shadow-xl">
          {navLinks.map((link) => (
            <a 
              key={link.name} 
              href={link.href}
              className="text-lg font-bold uppercase"
              onClick={() => setMobileMenuOpen(false)}
            >
              {link.name}
            </a>
          ))}
          <hr className="border-gray-200" />
          <button className="w-full py-3 font-bold border-2 border-black hover:bg-gray-100">LOGIN</button>
          <button className="w-full py-3 bg-yellow-400 text-black font-black border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">CRIAR CONTA</button>
        </div>
      )}
    </header>
  );
};

export default Header;