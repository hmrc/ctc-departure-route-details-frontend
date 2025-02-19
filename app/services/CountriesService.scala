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

package services

import config.Constants.DeclarationType.TIR
import connectors.ReferenceDataConnector
import models.reference.Country
import models.{Index, RichOptionalJsArray, SelectableList, UserAnswers}
import pages.external.DeclarationTypePage
import pages.routing.index.{CountryOfRoutingInCL147Page, CountryOfRoutingPage}
import pages.sections.routing.CountriesOfRoutingSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountriesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getDestinationCountries(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers.get(DeclarationTypePage) match {
      case Some(TIR) => getCountries("CountryCodesCommunity")
      case _         => getCountries("CountryCodesCommonTransit")
    }

  def getCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    getCountries("CountryCodesFullList")

  def getCountriesOfRouting(userAnswers: UserAnswers, indexToKeep: Index)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers
      .get(CountriesOfRoutingSection)
      .flatMapWithIndex {
        case (_, `indexToKeep`) => None
        case (_, index)         => userAnswers.get(CountryOfRoutingPage(index))
      } match {
      case countries => getCountries().map(_.filterNot(countries.contains))
    }

  def getAddressPostcodeBasedCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    getCountries("CountryAddressPostcodeBased")

  def isInCL112(countryId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    isCountryInCodeList("CountryCodesCTC", countryId)

  def isInCL147(countryId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    isCountryInCodeList("CountryCustomsSecurityAgreementArea", countryId)

  def isInCL010(countryId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    isCountryInCodeList("CountryCodesCommunity", countryId)

  def getOfficeOfTransitCountries(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers
      .get(CountriesOfRoutingSection)
      .flatMapWithIndex {
        case (_, index) => userAnswers.get(CountryOfRoutingPage(index))
      } match {
      case Nil       => getCountries()
      case countries => Future.successful(SelectableList(countries))
    }

  def getOfficeOfExitCountries(userAnswers: UserAnswers, countryOfDestination: Country)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers
      .get(CountriesOfRoutingSection)
      .flatMapWithIndex {
        case (_, index) =>
          userAnswers.get(CountryOfRoutingInCL147Page(index)).flatMap {
            case true =>
              userAnswers.get(CountryOfRoutingPage(index)).flatMap {
                case `countryOfDestination` => None
                case value                  => Some(value)
              }
            case false => None
          }
      } match {
      case Nil       => getCountries()
      case countries => Future.successful(SelectableList(countries))
    }

  private def getCountries(listName: String)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    referenceDataConnector
      .getCountries(listName)
      .map(_.resolve())
      .map(SelectableList(_))

  private def isCountryInCodeList(listName: String, countryId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    referenceDataConnector
      .getCountry(listName, countryId)
      .map(_.isDefined)

  def doesCountryRequireZip(country: Country)(implicit hc: HeaderCarrier): Future[Boolean] =
    referenceDataConnector
      .getCountriesWithoutZipCountry(country.code.code)
      .map(_.isDefined)
}
