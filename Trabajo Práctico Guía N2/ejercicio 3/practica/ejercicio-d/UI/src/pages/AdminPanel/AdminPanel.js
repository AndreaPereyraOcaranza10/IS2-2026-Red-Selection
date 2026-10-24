import React from 'react'
import { Admin, fetchUtils, Resource,withLifecycleCallbacks } from 'react-admin'
import simpleRestProvider from "ra-data-simple-rest";
import ProductList from './ProductList';
import EditProduct from './EditProduct';
import CreateProduct from './CreateProduct';
import CategoryList from './Category/CategoryList';
import CategoryEdit from './Category/CategoryEdit';
import { fileUploadAPI } from '../../api/fileUpload';

const httpClient = (url,options={})=>{

  const token = localStorage.getItem('authToken');
  if(!options.headers) options.headers = new Headers();
  options.headers.set('Authorization',`Bearer ${token}`);
  return fetchUtils.fetchJson(url,options);
}

const dataProvider = withLifecycleCallbacks(simpleRestProvider('http://localhost:8080/api',httpClient),[
  {
    resource:"products",
    beforeSave: async (params,dataProvider) =>{
      const requestBody = {
        ...params
      }
      const productResList = params?.productResources ?? [];
      let thumbnailUrl = typeof params?.thumbnail === 'string'
        ? params.thumbnail
        : params?.thumbnail?.src;

      if (params?.thumbnail?.rawFile) {
        const formData = new FormData();
        formData.append("file", params.thumbnail.rawFile);
        formData.append("fileName", params.thumbnail.rawFile.name);
        const thumbnailResponse = await fileUploadAPI(formData);
        thumbnailUrl = thumbnailResponse.url;
      }

      const newProductResList = await Promise.all(productResList.map(async (productResource)=>{
        const fileValue = productResource?.url;
        if (!fileValue?.rawFile) {
          return {
            ...productResource,
            url: typeof fileValue === 'string' ? fileValue : fileValue?.src,
          };
        }

        const formData = new FormData();
        formData.append("file", fileValue.rawFile);
        formData.append("fileName", fileValue.rawFile.name);
        const fileUploadRes = await fileUploadAPI(formData);
        return {
          ...productResource,
          url:fileUploadRes.url,
        };
      }));

      if (thumbnailUrl && !newProductResList.some((resource) => resource.isPrimary)) {
        newProductResList.push({
          name: params.name,
          url: thumbnailUrl,
          type: "image",
          isPrimary: true,
        });
      }
      if (newProductResList.length > 0 && !newProductResList.some((resource) => resource.isPrimary)) {
        newProductResList[0].isPrimary = true;
      }
      if (newProductResList.length === 0) {
        throw new Error("Agrega una imagen del producto antes de guardar.");
      }

      const request = {
        ...requestBody,
        thumbnail: thumbnailUrl,
        productResources:newProductResList
      }
      return request;
    }
  }
]);

export const AdminPanel = () => {
  return (
    <Admin dataProvider={dataProvider} basename='/admin'>
      <Resource name='products' list={ProductList} edit={EditProduct} create={CreateProduct}/>
      <Resource name='category' list={CategoryList} edit={CategoryEdit}/>
    </Admin>
  )
}
