<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<div class="screenlet">
    <div class="screenlet-title-bar">
        <h3>${uiLabelMap.PageTitleEditProductStoreSurveys}</h3>
    </div>
    <div class="screenlet-body">
        <table cellspacing="0" class="basic-table">
          <thead>
            <tr class="header-row">
              <th>${uiLabelMap.CommonType}</th>
              <th>${uiLabelMap.CommonName}</th>
              <th>${uiLabelMap.CommonSurveys}</th>
              <th>${uiLabelMap.ProductProduct}</th>
              <th>${uiLabelMap.ProductCategory}</th>
              <th>${uiLabelMap.CommonFromDate}</th>
              <th>${uiLabelMap.CommonSequenceNum}</th>
              <th>&nbsp;</th>
            </tr>
          </thead>
            <#assign alt_row = false>
            <#list productStoreSurveys as storeSurvey>
              <#assign surveyType = storeSurvey.getRelatedOne("SurveyApplType", false)>
              <#assign survey = storeSurvey.getRelatedOne("Survey", false)>
              <tr valign="middle"<#if alt_row>class="alternate-row"</#if>>
                <td>${surveyType.get("description",locale)}</td>
                <td>${storeSurvey.groupName!}</td>
                <td><a href="/content/control/EditSurvey?surveyId=${storeSurvey.surveyId}" class="button tiny">${survey.description?default("[" + survey.surveyId + "]")}</a></td>
                <td>${storeSurvey.productId?default("${uiLabelMap.CommonNA}")}</td>
                <td>${storeSurvey.productCategoryId?default("${uiLabelMap.CommonNA}")}</td>
                <td>${storeSurvey.fromDate!?string}</td>
                <td>${storeSurvey.sequenceNum!}</td>
                <td>
                  <form name="deleteProductStoreSurveyAppl_${storeSurvey_index}" method="post" action="<@ofbizUrl>deleteProductStoreSurveyAppl</@ofbizUrl>">
                    <input type="hidden" name="productStoreId" value="${productStoreId}" />
                    <input type="hidden" name="productStoreSurveyId" value="${storeSurvey.productStoreSurveyId}" />
                    <a href="javascript:document.deleteProductStoreSurveyAppl_${storeSurvey_index}.submit()" class="button tiny">${uiLabelMap.CommonDelete}</a>
                  </form>
                </td>
              </tr>
                <#-- toggle the row color -->
                <#assign alt_row = !alt_row>
            </#list>
        </table>
    </div>
</div>
<div class="screenlet">
    <div class="screenlet-title-bar">
        <h3>${uiLabelMap.PageTitleAddProductStoreSurveys}</h3>
    </div>
    <div class="screenlet-body">
        <form name="addSurvey" action="<@ofbizUrl>createProductStoreSurveyAppl</@ofbizUrl>" method="post">
            <input type="hidden" name="productStoreId" value="${productStoreId}" />
            <table cellspacing="0" class="basic-table">
              <tr>
                <td >${uiLabelMap.CommonType}</td>
                <td>
                  <select name="surveyApplTypeId">
                    <#list surveyApplTypes as type>
                      <option value="${type.surveyApplTypeId}">${type.get("description",locale)}</option>
                    </#list>
                  </select>
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.CommonGroup} ${uiLabelMap.CommonName}</td>
                <td>
                  <input type="text" size="20" name="groupName" />
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.CommonSurveys}</td>
                <td>
                  <select name="surveyId">
                    <#list surveys as survey>
                      <option value="${survey.surveyId}">${survey.description?default("[" + survey.surveyId + "]")}</option>
                    </#list>
                  </select>
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.ProductProductId}</td>
                <td>
                  <input type="text" size="20" name="productId" />
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.ProductCategoryId}</td>
                <td>
                  <@htmlTemplate.lookupField formName="addSurvey" name="productCategoryId" id="productCategoryId" fieldFormName="LookupProductCategory"/>
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.CommonFromDate}</td>
                <td>
                  <@htmlTemplate.renderDateTimeField name="fromDate" event="" action="" className=""  title="Format: yyyy-MM-dd HH:mm:ss.SSS" value="" size="25" maxlength="30" id="fromDate1" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.CommonThruDate}</td>
                <td>
                  <@htmlTemplate.renderDateTimeField name="thruDate" event="" action="" className=""  title="Format: yyyy-MM-dd HH:mm:ss.SSS" value="" size="25" maxlength="30" id="thruDate1" dateType="date" shortDateInput=false timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.ProductStoreSurveyTemplatePath}</td>
                <td>
                  <input type="text" size="30" name="surveyTemplate" />
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.ProductStoreSurveyResultTemplatePath}</td>
                <td>
                  <input type="text" size="30" name="resultTemplate" />
                </td>
              </tr>
              <tr>
                <td >${uiLabelMap.CommonSequenceNum}</td>
                <td>
                  <input type="text" size="5" name="sequenceNum" />
                </td>
              </tr>
              <tr>
                <td>&nbsp;</td>
                <td><input type="submit" class="smallSubmit" value="${uiLabelMap.CommonAdd}" /></td>
              </tr>
            </table>
        </form>
    </div>
</div>