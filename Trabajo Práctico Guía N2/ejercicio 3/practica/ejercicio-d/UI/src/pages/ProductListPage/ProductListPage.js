import React, { useEffect, useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import FilterIcon from '../../components/common/FilterIcon';
import Categories from '../../components/Filters/Categories';
import PriceFilter from '../../components/Filters/PriceFilter';
import ColorsFilter from '../../components/Filters/ColorsFilter';
import SizeFilter from '../../components/Filters/SizeFilter';
import ProductCard from './ProductCard';
import { getAllProducts } from '../../api/fetchProducts';
import { useDispatch, useSelector } from 'react-redux';
import { setLoading } from '../../store/features/common'
import { fetchCategories } from '../../api/fetchCategories'
import { loadCategories } from '../../store/features/category'
const MAX_PRICE = 500;
const ProductListPage = ({categoryType}) => {
  const { categoryCode } = useParams();
  const [searchParams] = useSearchParams();
  const categoryTypeId = searchParams.get('typeId');
  const selectedCategoryCode = categoryType || categoryCode?.toUpperCase();

  const categoryData = useSelector((state)=> state?.categoryState?.categories);
  const dispatch = useDispatch();
  const [products,setProducts] = useState([]);
  const [selectedTypes, setSelectedTypes] = useState(categoryTypeId ? [categoryTypeId] : []);
  const [priceRange, setPriceRange] = useState({min: 0, max: MAX_PRICE});
  const [selectedColors, setSelectedColors] = useState([]);
  const [selectedSizes, setSelectedSizes] = useState([]);

  useEffect(() => {
    setSelectedTypes(categoryTypeId ? [categoryTypeId] : []);
  }, [categoryTypeId]);

  const category = useMemo(()=>{
    return categoryData?.find(element => element?.code === selectedCategoryCode);
  },[categoryData, selectedCategoryCode]);

  useEffect(()=>{
    if (!categoryData?.length) {
      fetchCategories().then(result => {
        if (Array.isArray(result)) dispatch(loadCategories(result));
      });
    }
  }, [categoryData?.length, dispatch]);

  useEffect(()=>{
    if (!category?.id) return;
    dispatch(setLoading(true));
    getAllProducts(category?.id).then(res=>{
      setProducts(Array.isArray(res) ? res : []);
    }).catch(err=>{
      
    }).finally(()=>{
      dispatch(setLoading(false));
    })
    
  },[category?.id, dispatch]);

  const availableColors = useMemo(() => [...new Set(products.flatMap(product => (product.variants || []).map(variant => variant.color).filter(Boolean)))], [products]);
  const availableSizes = useMemo(() => [...new Set(products.flatMap(product => (product.variants || []).map(variant => variant.size).filter(Boolean)))], [products]);
  const maxPrice = useMemo(() => Math.max(500, ...products.map(product => Number(product.price) || 0)), [products]);
  const filteredProducts = useMemo(() => products.filter(product => {
    const variants = product.variants || [];
    const matchesType = selectedTypes.length === 0 || selectedTypes.includes(product.categoryTypeId);
    const matchesPrice = Number(product.price) >= priceRange.min && Number(product.price) <= priceRange.max;
    const matchesColor = selectedColors.length === 0 || variants.some(variant => selectedColors.includes(variant.color));
    const matchesSize = selectedSizes.length === 0 || variants.some(variant => selectedSizes.includes(variant.size));
    return matchesType && matchesPrice && matchesColor && matchesSize;
  }), [products, selectedTypes, priceRange, selectedColors, selectedSizes]);

  const toggleType = (typeId) => setSelectedTypes(current => current.includes(typeId)
    ? current.filter(id => id !== typeId)
    : [...current, typeId]);


  return (
    <div>
        <div className='flex'>
            <div className='w-[20%] p-[10px] border rounded-lg m-[20px]'>
                {/* Filters */}
                <div className='flex justify-between '>
                <p className='text-[16px] text-gray-600'>Filter</p>
                <FilterIcon />
                
                </div>
                <div>
                  {/* Product types */}
                <p className='text-[16px] text-black mt-5'>Categories</p>
                <Categories types={category?.categoryTypes} selectedTypes={selectedTypes} onChange={toggleType}/>
                <hr></hr>
                </div>
                  {/* Price */}
                  <PriceFilter range={priceRange} onChange={setPriceRange} maxPrice={MAX_PRICE}/>
                  <hr></hr>
                  {/* Colors */}
                  <ColorsFilter colors={availableColors} appliedColors={selectedColors} onChange={setSelectedColors}/>
                  <hr></hr>
                   {/* Sizes */}
                   <SizeFilter sizes={availableSizes} selectedSizes={selectedSizes} onChange={setSelectedSizes}/>
            </div>

            <div className='p-[15px]'>
            <p className='text-black text-lg'>{category?.description}</p>
                {/* Products */}
                <div className='pt-4 grid grid-cols-1 lg:grid-cols-3 md:grid-cols-2 gap-8 px-2'>
                {filteredProducts.map((item,index)=>(
                  <ProductCard key={item?.id+"_"+index} {...item} title={item?.name}/>
                ))}
                {filteredProducts.length === 0 && <p className='text-gray-500'>No products match these filters.</p>}
                </div>

            </div>

        </div>
    </div>
  )
}

export default ProductListPage
