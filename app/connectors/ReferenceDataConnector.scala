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
import connectors.ReferenceDataConnector.*
import models.reference.*
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Reads}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClientV2) extends Logging {

  private def get[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Responses[T]] =
    http
      .get(url)
      .setHeader(HeaderNames.Accept -> {
        val version = if (config.isPhase6Enabled) "2.0" else "1.0"
        s"application/vnd.hmrc.$version+json"
      })
      .execute[Responses[T]]

  private def getOne[T](url: URL)(implicit ec: ExecutionContext, hc: HeaderCarrier, reads: HttpReads[Responses[T]]): Future[Response[T]] =
    get[T](url).map(_.map(_.head))

  def getCountries(listName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[Country]] = {
    implicit val reads: Reads[Country] = Country.reads(config)
    val url                            = url"${config.referenceDataUrl}/lists/$listName"
    get[Country](url)
  }

  def getCountry(listName: String, countryId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[Country]] = {
    implicit val reads: Reads[Country] = Country.reads(config)
    val queryParameters                = Country.queryParams(countryId)(config)
    val url                            = url"${config.referenceDataUrl}/lists/$listName?$queryParameters"
    getOne[Country](url)
  }

  def getCountriesWithoutZipCountry(code: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[CountryCode]] = {
    implicit val reads: Reads[CountryCode] = CountryCode.reads(config)
    val queryParameters                    = CountryCode.queryParams(code)(config)
    val url                                = url"${config.referenceDataUrl}/lists/CountryWithoutZip?$queryParameters"
    getOne[CountryCode](url)
  }

  def getUnLocode(unLocode: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Response[UnLocode]] = {
    implicit val reads: Reads[UnLocode] = UnLocode.reads(config)
    val queryParameters                 = UnLocode.queryParams(unLocode)(config)
    val url                             = url"${config.referenceDataUrl}/lists/UnLocodeExtended?$queryParameters"
    getOne[UnLocode](url)
  }

  def getSpecificCircumstanceIndicators()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[SpecificCircumstanceIndicator]] = {
    implicit val reads: Reads[SpecificCircumstanceIndicator] = SpecificCircumstanceIndicator.reads(config)
    val url                                                  = url"${config.referenceDataUrl}/lists/SpecificCircumstanceIndicatorCode"
    get[SpecificCircumstanceIndicator](url)
  }

  def getTypesOfLocation()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationType]] = {
    implicit val reads: Reads[LocationType] = LocationType.reads(config)
    val url                                 = url"${config.referenceDataUrl}/lists/TypeOfLocation"
    get[LocationType](url)
  }

  def getQualifierOfTheIdentifications()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[LocationOfGoodsIdentification]] = {
    implicit val reads: Reads[LocationOfGoodsIdentification] = LocationOfGoodsIdentification.reads(config)
    val url                                                  = url"${config.referenceDataUrl}/lists/QualifierOfTheIdentification"
    get[LocationOfGoodsIdentification](url)
  }

  def getCustomsOfficesForCountryAndRole(
    countryCode: String,
    role: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Responses[CustomsOffice]] = {
    implicit val reads: Reads[List[CustomsOffice]] = CustomsOffice.listReads(config)
    val queryParameters                            = CustomsOffice.queryParameters(countryCodes = Seq(countryCode), roles = Seq(role))(config)
    val url                                        = url"${config.referenceDataUrl}/lists/CustomsOffices?$queryParameters"
    get[CustomsOffice](url)
  }

  implicit def responseHandlerGeneric[A](implicit reads: Reads[List[A]], order: Order[A]): HttpReads[Responses[A]] =
    (_: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          val json = if (config.isPhase6Enabled) response.json else response.json \ "data"
          json.validate[List[A]] match {
            case JsSuccess(Nil, _) =>
              Left(NoReferenceDataFoundException(url))
            case JsSuccess(head :: tail, _) =>
              Right(NonEmptySet.of(head, tail*))
            case JsError(errors) =>
              Left(JsResultException(errors))
          }
        case e =>
          logger.warn(s"[ReferenceDataConnector][responseHandlerGeneric] Reference data call returned $e")
          Left(Exception(s"[ReferenceDataConnector][responseHandlerGeneric] $e - ${response.body}"))
      }
}

object ReferenceDataConnector {

  type Responses[T] = Either[Exception, NonEmptySet[T]]
  type Response[T]  = Either[Exception, T]

  class NoReferenceDataFoundException(url: String) extends Exception(s"The reference data call was successful but the response body is empty: $url")
}
