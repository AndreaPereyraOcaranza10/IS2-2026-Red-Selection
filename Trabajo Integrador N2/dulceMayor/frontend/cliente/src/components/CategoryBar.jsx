import { jsx, jsxs } from "react/jsx-runtime";
import { Candy, Cookie, Donut, IceCreamBowl, Package, Popcorn } from "lucide-react";
const categories = [
  { name: "Ver todo", icon: Package },
  { name: "Alfajores", icon: Cookie },
  { name: "Chocolates", icon: Candy },
  { name: "Gomitas", icon: Donut },
  { name: "Caramelos", icon: IceCreamBowl },
  { name: "Chicles", icon: Popcorn }
];
export function CategoryBar({ active, onChange }) {
  return /* @__PURE__ */ jsx("div", { className: "category-bar", children: categories.map(({ name, icon: Icon }) => /* @__PURE__ */ jsxs("button", { className: active === name ? "category active" : "category", onClick: () => onChange(name), children: [
    /* @__PURE__ */ jsx(Icon, { size: 18 }),
    /* @__PURE__ */ jsx("span", { children: name })
  ] }, name)) });
}
