import React, { useEffect } from 'react'
import HeroSection from './components/HeroSection/HeroSection'
import NewArrivals from './components/Sections/NewArrivals'
import Category from './components/Sections/Categories/Category'
import content from './data/content.json';
import Footer from './components/Footer/Footer'
import { fetchCategories } from './api/fetchCategories';
import { useDispatch } from 'react-redux';
import { loadCategories } from './store/features/category';
import { setLoading } from './store/features/common';
import { useSelector } from 'react-redux';

const Shop = () => {

  const dispatch = useDispatch();
  const categories = useSelector(state => state.categoryState.categories);



  useEffect(()=>{
    //dispatch(setLoading(true));
    fetchCategories().then(res=>{
      dispatch(loadCategories(res));
    }).catch(err=>{

    }).finally(()=>{
      dispatch(setLoading(false));
    })
  },[dispatch]);

  return (
    <>
      <HeroSection />
      <NewArrivals />
      {categories?.map(category => <Category key={category.id} category={category} />)}
      <Footer content={content?.footer}/>
    </>
  )
}

export default Shop
