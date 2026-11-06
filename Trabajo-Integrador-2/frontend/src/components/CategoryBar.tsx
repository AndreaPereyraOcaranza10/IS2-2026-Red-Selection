import { Candy, Cookie, Donut, IceCreamBowl, Package, Popcorn } from 'lucide-react';

const categories = [
  { name: 'Ver todo', icon: Package }, { name: 'Alfajores', icon: Cookie }, { name: 'Chocolates', icon: Candy }, { name: 'Gomitas', icon: Donut }, { name: 'Caramelos', icon: IceCreamBowl }, { name: 'Chicles', icon: Popcorn },
];
export function CategoryBar({ active, onChange }: { active: string; onChange: (category: string) => void }) {
  return <div className="category-bar">{categories.map(({ name, icon: Icon }) => <button key={name} className={active === name ? 'category active' : 'category'} onClick={() => onChange(name)}><Icon size={18} /><span>{name}</span></button>)}</div>;
}
