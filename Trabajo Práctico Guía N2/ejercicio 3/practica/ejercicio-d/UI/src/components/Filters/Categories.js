import React from 'react'

const Categories = ({types = [], selectedTypes = [], onChange}) => {
  return (
    <div >
      {types.map(type=>{
        return (
          <div key={type.id} className='flex items-center p-1'>
            <input id={type.id} type='checkbox' checked={selectedTypes.includes(type.id)} onChange={() => onChange(type.id)} className='border rounded-xl w-4 h-4 accent-black text-black'/>
            <label htmlFor={type.id} className='px-2 text-[14px] text-gray-600'>{type?.name}</label>
          </div>
        )
      })}
    </div>
  )
}

export default Categories
