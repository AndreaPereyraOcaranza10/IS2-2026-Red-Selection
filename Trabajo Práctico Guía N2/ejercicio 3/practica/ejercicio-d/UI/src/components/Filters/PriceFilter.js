import React from 'react'
import RangeSlider from 'react-range-slider-input';
import 'react-range-slider-input/dist/style.css';
import './PriceFilter.css';

const PriceFilter = ({range, onChange, maxPrice = 500}) => {
    const sliderMax = Math.max(1, Math.ceil(maxPrice));
  return (
    <div className='flex flex-col mb-4'>
        <p className='text-[16px] text-black mt-5 mb-5'>Price (USD)</p>
        <RangeSlider className={'custom-range-slider'} min={0} max={sliderMax} value={[range.min,range.max]} onInput={(values) => onChange({min: values[0], max: values[1]})}/>

        <div className='flex justify-between'>
            <div className='border rounded-lg h-8 mt-4 max-w-[50%] w-[40%] flex items-center'><p className='pl-4 text-gray-600'>$</p> <input type='number' value={range.min} className='outline-none px-4 text-gray-600' readOnly aria-label='Minimum price'/></div>
            <div className='border rounded-lg h-8 mt-4 max-w-[50%] w-[40%] flex items-center'><p className='pl-4 text-gray-600'>$</p> <input type='number' value={range.max} className='outline-none px-4 text-gray-600' readOnly aria-label='Maximum price'/></div>
        </div>
    </div>
  )
}

export default PriceFilter
