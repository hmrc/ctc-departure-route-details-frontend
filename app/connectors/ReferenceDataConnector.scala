/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import cats.Order
import cats.data.NonEmptySet
import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import models.reference._
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[Country]] = {
    val url = url"${config.referenceDataUrl}/lists/$listName"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[Country]]
  }

  def getCountriesWithoutZip()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CountryCode]] = {
    val url = url"${config.referenceDataUrl}/lists/CountryWithoutZip"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CountryCode]]
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[UnLocode] = {
    val url = url"${config.referenceDataUrl}/lists/UnLocodeExtended"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.unLocodeExtendedCode" -> unLocode))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[UnLocode]]
      .map(_.head)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[SpecificCircumstanceIndicator]] = {
    val url = url"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[SpecificCircumstanceIndicator]]
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[LocationType]] = {
    val url = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationType]]
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[LocationOfGoodsIdentification]] = {
    val url = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    http
      .get(url)
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[LocationOfGoodsIdentification]]
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  def getCustomsOfficesForCountryAndRole(
    countryCode: String,
    role: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[NonEmptySet[CustomsOffice]] = {
    val url = url"${config.referenceDataUrl}/lists/CustomsOffices"
    http
      .get(url)
      .transform(_.withQueryStringParameters("data.countryId" -> countryCode, "data.roles.role" -> role))
      .setHeader(version2Header: _*)
      .execute[NonEmptySet[CustomsOffice]]
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[A], order: Order[A]): HttpReads[NonEmptySet[A]] =
    (_: String, url: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          (response.json \ "data").validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              throw new NoReferenceDataFoundException(url)
            case JsSuccess(head :: tail, _) =>
              NonEmptySet.of(head, tail: _*)
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          throw new Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}")
      }
    }
}

object ReferenceDataConnector {

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
