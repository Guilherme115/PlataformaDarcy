import React from 'react';
import Header from './components/Header';
import Hero from './components/Hero';
import Ticker from './components/Ticker';
import Stats from './components/Stats';
import Philosophy from './components/Philosophy';
import TechIllustration from './components/TechIllustration';
import Features from './components/Features';
import Contribution from './components/Contribution';
import Pricing from './components/Pricing';
import Footer from './components/Footer';

function App() {
  return (
    <div className="min-h-screen flex flex-col font-sans text-black selection:bg-yellow-400 selection:text-black">
      <Header />
      <main className="flex-grow">
        <Hero />
        <Ticker />
        <Stats />
        <Philosophy />
        <TechIllustration />
        <Contribution />
        <Features />
        <Pricing />
      </main>
      <Footer />
    </div>
  );
}

export default App;