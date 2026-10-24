import React from 'react'
import SectionHeading from '../SectionsHeading/SectionHeading'
import Card from '../../Card/Card'

const imageByType = {
  men: {
    shirts: '/images/category-men-shirts.jpg',
    jeans: '/images/category-men-jeans.jpg',
    hoodies: '/images/category-men-hoodies.png',
    hoodie: '/images/category-men-hoodies.png',
    sweatshirt: '/images/category-men-hoodies.png',
    tshirts: '/images/category-men-tshirts.png',
    tshirt: '/images/category-men-tshirts.png',
  },
  women: {
    hoodies: '/images/category-women-hoodies.jpg',
    hoodie: '/images/category-women-hoodies.jpg',
    sweatshirt: '/images/category-women-hoodies.jpg',
    coats: '/images/category-women-coats.jpg',
    parka: '/images/category-women-coats.jpg',
    tees: '/images/category-women-tshirts.png',
    tshirts: '/images/category-women-tshirts.png',
    tshirt: '/images/category-women-tshirts.png',
    shorts: '/images/category-women-shorts.jpg',
  },
}

const normalize = value => value?.toLowerCase().replace(/[^a-z]/g, '') ?? ''

const getTypeImage = (category, type) => {
  const group = normalize(category.code || category.name)
  const typeName = normalize(type.code || type.name)
  const images = imageByType[group] || {}
  const match = Object.keys(images).find(key => typeName.includes(key) || key.includes(typeName))
  return match ? images[match] : group === 'women'
    ? '/images/category-women-tshirts.png'
    : '/images/category-men-tshirts.png'
}

const Category = ({ category }) => {
  const types = category?.categoryTypes ?? []

  return (
    <>
      <SectionHeading title={`Categories For ${category?.name ?? ''}`} />
      <div className='flex items-center px-8 flex-wrap'>
        {types.map(type => (
          <Card
            key={type.id ?? type.code ?? type.name}
            title={type.name}
            description={type.description || 'Explore Now!'}
            imagePath={getTypeImage(category, type)}
            to={`/shop/${encodeURIComponent(category.code?.toLowerCase() || category.name?.toLowerCase())}?typeId=${encodeURIComponent(type.id)}`}
            actionArrow={true}
            height={'240px'}
            width={'200px'}
          />
        ))}
      </div>
    </>
  )
}

export default Category
