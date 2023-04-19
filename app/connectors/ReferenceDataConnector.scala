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

import config.FrontendAppConfig
import models.reference._
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import sttp.model.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReferenceDataConnector @Inject() (config: FrontendAppConfig, http: HttpClient) extends Logging {

  def getCountries(queryParameters: Seq[(String, String)])(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/countries"
    http.GET[Seq[Country]](serviceUrl, queryParameters, headers = version2Header)
  }

  def getCustomsOfficesOfTransitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-offices/${countryCode.code}?role=TRA"
    http.GET[Seq[CustomsOffice]](serviceUrl, headers = version2Header)
  }

  def getCustomsOfficesOfDestinationForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-offices/${countryCode.code}?role=DES"
    http.GET[Seq[CustomsOffice]](serviceUrl, headers = version2Header)
  }

  def getCustomsOfficesOfExitForCountry(
    countryCode: CountryCode
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-offices/${countryCode.code}?role=EXT"
    http.GET[Seq[CustomsOffice]](serviceUrl, headers = version2Header)
  }

  def getCustomsOfficesOfDepartureForCountry(
    countryCode: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CustomsOffice]] = {
    val serviceUrl = s"${config.referenceDataUrl}/customs-offices/$countryCode?role=DEP"
    http.GET[Seq[CustomsOffice]](serviceUrl, headers = version2Header)
  }

  def getCustomsSecurityAgreementAreaCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/country-customs-office-security-agreement-area"
    http.GET[Seq[Country]](serviceUrl, headers = version2Header)
  }

  def getCountryCodesCTC()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/country-codes-ctc"
    http.GET[Seq[Country]](serviceUrl, headers = version2Header)
  }

  def getAddressPostcodeBasedCountries()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[Country]] = {
    val serviceUrl = s"${config.referenceDataUrl}/country-address-postcode-based"
    http.GET[Seq[Country]](serviceUrl, headers = version2Header)
  }

  def getCountriesWithoutZip()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[CountryCode]] = {
    val serviceUrl = s"${config.referenceDataUrl}/country-without-zip"
    http.GET[Seq[CountryCode]](serviceUrl, headers = version2Header)
  }

  def getUnLocodes()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Seq[UnLocode]] = {
    val serviceUrl = s"${config.referenceDataUrl}/un-locodes"
    http.GET[Seq[UnLocode]](serviceUrl, headers = version2Header)
  }

  private def version2Header: Seq[(String, String)] = Seq(
    HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"
  )

  implicit val responseHandlerCustomsOfficeList: HttpReads[Seq[CustomsOffice]] =
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          response.json
            .as[Seq[CustomsOffice]]
        case NOT_FOUND =>
          Nil
        case other =>
          logger.info(s"[ReferenceDataConnector][getCustomsOfficesForCountry] Invalid downstream status $other")
          throw new IllegalStateException(s"Invalid Downstream Status $other")
      }
}
