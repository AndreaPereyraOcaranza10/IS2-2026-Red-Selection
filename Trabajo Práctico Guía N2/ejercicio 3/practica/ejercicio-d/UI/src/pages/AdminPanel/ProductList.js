import React from 'react'
import { Datagrid, FunctionField, ImageField, List, TextField } from 'react-admin'
import { formatUSD } from '../../utils/currency'

const ProductList = () => {
  return (
    <List>
        <Datagrid>
            <TextField disabled source='id'/>
            <ImageField source='thumbnail'/>
            <TextField source='name'/>
            <TextField source='brand'/>\
            <TextField source='description'/>
            <FunctionField label='Price (USD)' render={record => formatUSD(record.price)} />
            <TextField source='slug'/>
        </Datagrid>
    </List>
  )
}

export default ProductList
