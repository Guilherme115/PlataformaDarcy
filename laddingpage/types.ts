export interface PricingTier {
  name: string;
  price: string;
  period?: string;
  features: string[];
  notIncluded?: string[];
  cta: string;
  highlight?: boolean;
}

export interface Testimonial {
  quote: string;
  author: string;
  role: string;
  icon?: string;
  bgColor?: string;
  textColor?: string;
}

export interface Stat {
  value: string;
  label: string;
  subLabel?: string;
}