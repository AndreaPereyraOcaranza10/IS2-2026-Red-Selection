import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import SectionHeading from './SectionsHeading/SectionHeading'
import Carousel from 'react-multi-carousel'
import { responsive } from '../../utils/Section.constants'
import { getAllProducts } from '../../api/fetchProducts'
import './NewArrivals.css'
import { formatUSD } from '../../utils/currency';

const NewArrivals = () => {
  const [products, setProducts] = useState([])

  useEffect(() => {
    getAllProducts().then(result => {
      const arrivals = (Array.isArray(result) ? result : []).filter(product =>
        product?.isNewArrival === true || product?.newArrival === true
      )
      setProducts(arrivals.slice(0, 12))
    })
  }, [])

  return (
    <>
      <SectionHeading title='New Arrivals' />
      {products.length > 0 ? (
        <Carousel
          responsive={responsive}
          autoPlay={false}
          swipeable
          draggable
          showDots={false}
          infinite={false}
          partialVisible={false}
          itemClass='react-slider-custom-item'
          className='px-8'
        >
          {products.map(product => (
            <Link key={product.id} to={`/product/${product.slug}`} className='flex flex-col p-4 text-inherit no-underline'>
              <img
                className='h-[220px] w-full rounded-lg border object-cover'
                src={product.thumbnail}
                alt={product.name}
              />
              <p className='pt-2 text-[16px]'>{product.name}</p>
              <p className='text-sm text-gray-600'>{formatUSD(product.price)}</p>
            </Link>
          ))}
        </Carousel>
      ) : (
        <p className='px-10 pb-6 text-gray-600'>Todavía no hay productos marcados como novedades.</p>
      )}
    </>
  )
}

export default NewArrivals
