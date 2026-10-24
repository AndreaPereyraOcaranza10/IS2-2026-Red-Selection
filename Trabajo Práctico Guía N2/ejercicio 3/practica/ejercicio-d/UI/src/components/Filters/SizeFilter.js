import React from 'react'

const SizeFilter = ({sizes = [], hidleTitle, selectedSizes = [], onChange}) => {
  const toggle = (item) => onChange(selectedSizes.includes(item)
    ? selectedSizes.filter(size => size !== item)
    : [...selectedSizes, item]);

  return (
    <div className={`flex flex-col ${hidleTitle?'':'mb-4'}`}>
        {!hidleTitle && <p className='text-[16px] text-black mt-5 mb-5'>Size</p>}
        <div className='flex flex-wrap px-2'>
            {sizes.map((item,index)=> {
              return (
                <div key={index} className='flex flex-col mr-2'>
                  <button type='button' aria-pressed={selectedSizes.includes(item)} className='w-[50px] border text-center mb-4 rounded-lg mr-4 cursor-pointer
                   hover:scale-110 bg-white border-gray-500 text-gray-500' style={selectedSizes.includes(item)?{
                    background:'black',
                    color:'white'
                   }:{}} onClick={()=> toggle(item)}>{item}</button>
                  
                  </div>
              )
            })}
        </div>
    </div>
  )
}

export default SizeFilter
