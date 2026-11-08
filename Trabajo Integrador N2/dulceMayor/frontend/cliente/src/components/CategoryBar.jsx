import { jsx, jsxs } from "react/jsx-runtime";
import { Candy, Cookie, Donut, IceCreamBowl, Package, Popcorn } from "lucide-react";
const categoryIcons = { alfajores: Cookie, chocolates: Candy, gomitas: Donut, caramelos: IceCreamBowl, chicles: Popcorn };
export function CategoryBar({ active, onChange, categories = [] }) {
  const items = [{ name: "Ver todo", icon: Package }, ...categories.map((name) => ({ name, icon: categoryIcons[name.toLocaleLowerCase("es")] || Package }))];
  return /* @__PURE__ */ jsx("div", { className: "category-bar", children: items.map(({ name, icon: Icon }) => /* @__PURE__ */ jsxs("button", { className: active === name ? "category active" : "category", onClick: () => onChange(name), children: [
    /* @__PURE__ */ jsx(Icon, { size: 18 }),
    /* @__PURE__ */ jsx("span", { children: name })
  ] }, name)) });
}
