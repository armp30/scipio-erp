/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.product.product;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.GeneralRuntimeException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilCodec;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.content.content.ContentLangUtil;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.content.content.ContentWrapper;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelUtil;
import org.ofbiz.entity.util.EntityQuery;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;

/**
 * Product Promo Content Worker: gets product promo content to display
 */
public class ProductPromoContentWrapper implements ContentWrapper {

    public static final String module = ProductPromoContentWrapper.class.getName();
    public static final String SEPARATOR = "::";    // cache key separator

    private static final UtilCache<String, String> productPromoContentCache = UtilCache.createUtilCache("product.promo.content.rendered", true);

    public static ProductPromoContentWrapper makeProductPromoContentWrapper(GenericValue productPromo, HttpServletRequest request) {
        return new ProductPromoContentWrapper(productPromo, request);
    }

    LocalDispatcher dispatcher;
    protected GenericValue productPromo;
    protected Locale locale;
    protected String mimeTypeId;

    public ProductPromoContentWrapper(LocalDispatcher dispatcher, GenericValue productPromo, Locale locale, String mimeTypeId) {
        this.dispatcher = dispatcher;
        this.productPromo = productPromo;
        this.locale = locale;
        this.mimeTypeId = mimeTypeId;
    }

    public ProductPromoContentWrapper(GenericValue productPromo, HttpServletRequest request) {
        this.dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        this.productPromo = productPromo;
        this.locale = UtilHttp.getLocale(request);
        this.mimeTypeId = "text/html";
    }

    // SCIPIO: changed return type, parameter, and encoding largely removed
    public String get(String productPromoContentTypeId, String encoderType) {
        if (UtilValidate.isEmpty(this.productPromo)) {
            Debug.logWarning("Tried to get ProductPromoContent for type [" + productPromoContentTypeId + "] but the productPromo field in the ProductPromoContentWrapper is null", module);
            return null;
        }
        return getProductPromoContentAsText(this.productPromo, productPromoContentTypeId, locale, mimeTypeId, null, null, this.productPromo.getDelegator(), dispatcher, encoderType);
    }
    
    /**
     * SCIPIO: Version of overload that performs NO encoding. In most cases templates should do the encoding.
     */
    public String get(String productPromoContentTypeId) {
        if (UtilValidate.isEmpty(this.productPromo)) {
            Debug.logWarning("Tried to get ProductPromoContent for type [" + productPromoContentTypeId + "] but the productPromo field in the ProductPromoContentWrapper is null", module);
            return null;
        }
        return getProductPromoContentAsText(this.productPromo, productPromoContentTypeId, locale, mimeTypeId, null, null, this.productPromo.getDelegator(), dispatcher, "raw");
    }

    public static String getProductPromoContentAsText(GenericValue productPromo, String productPromoContentTypeId, HttpServletRequest request, String encoderType) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        return getProductPromoContentAsText(productPromo, productPromoContentTypeId, UtilHttp.getLocale(request), "text/html", null, null, productPromo.getDelegator(), dispatcher, encoderType);
    }

    public static String getProductContentAsText(GenericValue productPromo, String productPromoContentTypeId, Locale locale, LocalDispatcher dispatcher, String encoderType) {
        return getProductPromoContentAsText(productPromo, productPromoContentTypeId, locale, null, null, null, null, dispatcher, encoderType);
    }

    public static String getProductPromoContentAsText(GenericValue productPromo, String productPromoContentTypeId, Locale locale, String mimeTypeId, String partyId, String roleTypeId, Delegator delegator, LocalDispatcher dispatcher, String encoderType) {
        if (UtilValidate.isEmpty(productPromo)) {
            return null;
        }

        UtilCodec.SimpleEncoder encoder = ContentLangUtil.getContentWrapperSanitizer(encoderType);
        String candidateFieldName = ModelUtil.dbNameToVarName(productPromoContentTypeId);
        /* caching: there is one cache created, "product.promo.content"  Each productPromo's content is cached with a key of
         * contentTypeId::locale::mimeType::productPromoId, or whatever the SEPARATOR is defined above to be.
         */
        String cacheKey = productPromoContentTypeId + SEPARATOR + locale + SEPARATOR + mimeTypeId + SEPARATOR + productPromo.get("productPromoId") + SEPARATOR + encoderType + SEPARATOR + delegator;
        try {
            String cachedValue = productPromoContentCache.get(cacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }

            Writer outWriter = new StringWriter();
            getProductPromoContentAsText(null, productPromo, productPromoContentTypeId, locale, mimeTypeId, partyId, roleTypeId, delegator, dispatcher, outWriter);
            String outString = outWriter.toString();
            if (outString.length() > 0) {
                return productPromoContentCache.putIfAbsentAndGet(cacheKey, encoder.encode(outString));
            } else {
                String candidateOut = productPromo.getModelEntity().isField(candidateFieldName) ? productPromo.getString(candidateFieldName): "";
                return candidateOut == null? "" : encoder.encode(candidateOut);
            }
        } catch (GeneralException e) {
            Debug.logError(e, "Error rendering ProductPromoContent, inserting empty String", module);
            String candidateOut = productPromo.getModelEntity().isField(candidateFieldName) ? productPromo.getString(candidateFieldName): "";
            return candidateOut == null? "" : encoder.encode(candidateOut);
        } catch (IOException e) {
            Debug.logError(e, "Error rendering ProductPromoContent, inserting empty String", module);
            String candidateOut = productPromo.getModelEntity().isField(candidateFieldName) ? productPromo.getString(candidateFieldName): "";
            return candidateOut == null? "" : encoder.encode(candidateOut);
        }
    }

    public static void getProductPromoContentAsText(String productPromoId, GenericValue productPromo, String productPromoContentTypeId, Locale locale, String mimeTypeId, String partyId, String roleTypeId, Delegator delegator, LocalDispatcher dispatcher, Writer outWriter) throws GeneralException, IOException {
        if (UtilValidate.isEmpty(productPromoId) && UtilValidate.isNotEmpty(productPromo)) {
            productPromoId = productPromo.getString("productPromoId");
        }

        if (UtilValidate.isEmpty(delegator) && UtilValidate.isNotEmpty(productPromo)) {
            delegator = productPromo.getDelegator();
        }

        if (UtilValidate.isEmpty(mimeTypeId)) {
            mimeTypeId = "text/html";
        }

        if (UtilValidate.isEmpty(delegator)) {
            throw new GeneralRuntimeException("Unable to find a delegator to use!");
        }

        String candidateFieldName = ModelUtil.dbNameToVarName(productPromoContentTypeId);
        ModelEntity productModel = delegator.getModelEntity("ProductPromo");
        if (productModel.isField(candidateFieldName)) {
            if (UtilValidate.isEmpty(productPromo)) {
                productPromo = EntityQuery.use(delegator).from("ProductPromo").where("productPromoId", productPromoId).cache().queryOne();
            }
            if (UtilValidate.isNotEmpty(productPromo)) {
                String candidateValue = productPromo.getString(candidateFieldName);
                if (UtilValidate.isNotEmpty(candidateValue)) {
                    outWriter.write(candidateValue);
                    return;
                } 
            }
        }

        List<EntityExpr> exprs = new ArrayList<EntityExpr>();
        exprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
        exprs.add(EntityCondition.makeCondition("productPromoContentTypeId", EntityOperator.EQUALS, productPromoContentTypeId));

        List<GenericValue> productPromoContentList = EntityQuery.use(delegator).from("ProductPromoContent").where(EntityCondition.makeCondition(exprs, EntityOperator.AND)).orderBy("-fromDate").cache(true).queryList();
        GenericValue productPromoContent = null;
        if (UtilValidate.isNotEmpty(productPromoContentList)) {
            productPromoContent = EntityUtil.getFirst(EntityUtil.filterByDate(productPromoContentList));
        }

        if (UtilValidate.isNotEmpty(productPromoContent)) {
            // when rendering the product promo content, always include the ProductPromo and ProductPromoContent records that this comes from
            Map<String, Object> inContext = FastMap.newInstance();
            inContext.put("productPromo", productPromo);
            inContext.put("productPromoContent", productPromoContent);
            ContentWorker.renderContentAsText(dispatcher, delegator, productPromoContent.getString("contentId"), outWriter, inContext, locale, mimeTypeId, partyId, roleTypeId, true);
        }
    }
}
