import React, { useState, useEffect } from 'react';
import { BookPage } from './components/BookPage';
import { initialPages } from './data/initialState';
import { PageContent, PageLayout } from './types';
import { Save, Plus, Trash2, Layout, BookOpen, Monitor, RefreshCw } from 'lucide-react';

export default function App() {
  // Estado das Páginas (Carrega do LocalStorage ou usa o Inicial)
  const [pages, setPages] = useState<PageContent[]>(() => {
    const saved = localStorage.getItem('darcy_book_pages');
    return saved ? JSON.parse(saved) : initialPages;
  });

  const [activePageIndex, setActivePageIndex] = useState(0);
  const [scale, setScale] = useState(0.6); // Scale inicial para caber na tela
  
  // Salvar no LocalStorage sempre que alterar
  useEffect(() => {
    localStorage.setItem('darcy_book_pages', JSON.stringify(pages));
  }, [pages]);

  const activePage = pages[activePageIndex];

  // --- ACTIONS ---

  const handleUpdatePage = (field: keyof PageContent, value: any) => {
    const newPages = [...pages];
    newPages[activePageIndex] = { ...newPages[activePageIndex], [field]: value };
    setPages(newPages);
  };

  const handleAddPage = () => {
    const newPage: PageContent = {
      id: `page-${Date.now()}`,
      title: 'Nova Página',
      module: 'Módulo Novo',
      pageNumber: pages.length + 1,
      layout: 'standard',
      htmlContent: `<p class="font-serif text-lg">Comece a escrever aqui...</p>`
    };
    setPages([...pages, newPage]);
    setActivePageIndex(pages.length); // Vai para a nova página
  };

  const handleDeletePage = () => {
    if (pages.length <= 1) return alert("Você precisa ter pelo menos uma página.");
    if (confirm("Tem certeza que deseja excluir esta página?")) {
        const newPages = pages.filter((_, i) => i !== activePageIndex);
        setPages(newPages);
        setActivePageIndex(Math.max(0, activePageIndex - 1));
    }
  };

  const handleReset = () => {
      if(confirm("Isso apagará todo seu trabalho e restaurará os templates originais. Confirmar?")) {
          setPages(initialPages);
          setActivePageIndex(0);
      }
  }

  return (
    <div className="h-screen w-screen bg-[#111] flex overflow-hidden text-gray-200 font-sans">
      
      {/* --- SIDEBAR (Lista de Páginas) --- */}
      <div className="w-64 bg-[#1a1a1a] border-r border-[#333] flex flex-col flex-shrink-0">
        <div className="p-4 border-b border-[#333]">
            <h1 className="font-black text-darcy-yellow tracking-tighter text-xl">DARCY STUDIO</h1>
            <p className="text-xs text-gray-500 uppercase tracking-widest mt-1">Editor de Livros</p>
        </div>
        
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {pages.map((page, idx) => (
                <div 
                    key={page.id}
                    onClick={() => setActivePageIndex(idx)}
                    className={`p-3 rounded-md cursor-pointer text-sm border transition-all ${
                        idx === activePageIndex 
                        ? 'bg-darcy-yellow text-black border-darcy-yellow font-bold shadow-[2px_2px_0px_0px_white]' 
                        : 'bg-[#222] border-[#333] hover:border-gray-500 text-gray-400'
                    }`}
                >
                    <div className="flex justify-between items-center mb-1">
                        <span className="text-[10px] uppercase tracking-widest opacity-70">{page.layout}</span>
                        <span className="text-[10px] bg-black/20 px-1 rounded">Pág {page.pageNumber}</span>
                    </div>
                    <div className="truncate">{page.title}</div>
                </div>
            ))}
        </div>

        <div className="p-4 border-t border-[#333] grid grid-cols-2 gap-2">
            <button onClick={handleAddPage} className="flex items-center justify-center gap-2 bg-white text-black py-2 rounded text-xs font-bold hover:bg-gray-200 transition-colors col-span-2">
                <Plus size={14} /> NOVA PÁGINA
            </button>
            <button onClick={handleReset} className="flex items-center justify-center gap-2 bg-[#333] text-gray-400 py-2 rounded text-xs font-bold hover:bg-red-900 hover:text-white transition-colors col-span-2">
                <RefreshCw size={14} /> RESETAR TUDO
            </button>
        </div>
      </div>

      {/* --- EDITOR DE CÓDIGO (Centro) --- */}
      <div className="flex-1 flex flex-col min-w-[400px] border-r border-[#333]">
        {/* Toolbar da Página Ativa */}
        <div className="h-16 border-b border-[#333] bg-[#1a1a1a] flex items-center px-4 gap-4">
            <div className="flex flex-col">
                <label className="text-[9px] text-gray-500 uppercase font-bold">Título da Página</label>
                <input 
                    type="text" 
                    value={activePage.title} 
                    onChange={(e) => handleUpdatePage('title', e.target.value)}
                    className="bg-transparent border-b border-gray-600 focus:border-darcy-yellow outline-none text-white font-bold text-sm w-40"
                />
            </div>
            <div className="flex flex-col">
                <label className="text-[9px] text-gray-500 uppercase font-bold">Módulo / Cabeçalho</label>
                <input 
                    type="text" 
                    value={activePage.module} 
                    onChange={(e) => handleUpdatePage('module', e.target.value)}
                    className="bg-transparent border-b border-gray-600 focus:border-darcy-yellow outline-none text-white text-sm w-40"
                />
            </div>
            <div className="flex flex-col">
                <label className="text-[9px] text-gray-500 uppercase font-bold">Layout</label>
                <select 
                    value={activePage.layout} 
                    onChange={(e) => handleUpdatePage('layout', e.target.value)}
                    className="bg-[#222] text-white text-xs p-1 rounded border border-[#444]"
                >
                    <option value="standard">Padrão (Texto + Lateral)</option>
                    <option value="cover">Capa (Full)</option>
                    <option value="toc">Sumário</option>
                </select>
            </div>
            <div className="flex flex-col">
                <label className="text-[9px] text-gray-500 uppercase font-bold">Nº Pág</label>
                <input 
                    type="number" 
                    value={activePage.pageNumber} 
                    onChange={(e) => handleUpdatePage('pageNumber', parseInt(e.target.value))}
                    className="bg-transparent border-b border-gray-600 focus:border-darcy-yellow outline-none text-white text-sm w-12 text-center"
                />
            </div>
            
            <div className="flex-1"></div>
            
            <button onClick={handleDeletePage} className="text-red-500 hover:bg-red-900/50 p-2 rounded">
                <Trash2 size={18} />
            </button>
        </div>

        {/* Área de Texto (Code Editor) */}
        <div className="flex-1 relative bg-[#0d0d0d]">
             <span className="absolute top-0 right-4 bg-darcy-yellow text-black text-[9px] font-bold px-2 py-1 rounded-b">HTML EDIT</span>
             <textarea 
                value={activePage.htmlContent || ''}
                onChange={(e) => handleUpdatePage('htmlContent', e.target.value)}
                className="w-full h-full bg-transparent text-gray-300 font-mono text-sm p-6 outline-none resize-none leading-relaxed"
                spellCheck={false}
             />
        </div>
      </div>

      {/* --- PREVIEW (Direita) --- */}
      <div className="w-[50%] bg-[#222] relative flex flex-col items-center justify-center overflow-hidden">
        <div className="absolute top-4 right-4 z-50 bg-black/50 p-2 rounded-full flex gap-2 backdrop-blur-md">
             <button onClick={() => setScale(s => Math.max(0.3, s - 0.1))} className="text-white hover:text-darcy-yellow"><Layout size={16} /></button>
             <span className="text-xs font-mono text-white pt-1">{Math.round(scale * 100)}%</span>
             <button onClick={() => setScale(s => Math.min(1.2, s + 0.1))} className="text-white hover:text-darcy-yellow"><BookOpen size={16} /></button>
        </div>

        <div className="overflow-auto w-full h-full flex items-center justify-center p-10 bg-grid-pattern">
            <BookPage data={activePage} scale={scale} />
        </div>
      </div>

    </div>
  );
}