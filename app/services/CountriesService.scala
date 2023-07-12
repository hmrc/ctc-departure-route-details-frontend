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

import connectors.ReferenceDataConnector
import models.SelectableList.countriesOfRoutingReads
import models.reference.{Country, CountryCode}
import models.{DeclarationType, RichOptionalJsArray, SelectableList, UserAnswers}
import pages.external.DeclarationTypePage
import pages.sections.routing.CountriesOfRoutingSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountriesService @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) {

  def getDestinationCountries(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers.get(DeclarationTypePage) match {
      case Some(DeclarationType.Option4) => getCommunityCountries()
      case _                             => getTransitCountries()
    }

  def getCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    getCountries("CountryCodesFullList")

  def getTransitCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    getCountries("CountryCodesCommonTransit")

  def getAddressPostcodeBasedCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    referenceDataConnector
      .getAddressPostcodeBasedCountries()
      .map(sort)

  def getCommunityCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    getCountries("CountryCodesCommunity")

  def getCustomsSecurityAgreementAreaCountries()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    referenceDataConnector
      .getCustomsSecurityAgreementAreaCountries()
      .map(sort)

  def getCountryCodesCTC()(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    referenceDataConnector
      .getCountryCodesCTC()
      .map(sort)

  def getCountriesWithoutZip()(implicit hc: HeaderCarrier): Future[Seq[CountryCode]] =
    referenceDataConnector
      .getCountriesWithoutZip()

  def getOfficeOfTransitCountries(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers.get(CountriesOfRoutingSection).validate(countriesOfRoutingReads) match {
      case Some(x) if x.values.nonEmpty => Future.successful(x)
      case _                            => getCountries()
    }

  def getOfficeOfExitCountries(userAnswers: UserAnswers, countryOfDestination: Country)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    userAnswers.get(CountriesOfRoutingSection).validate(countriesOfRoutingReads).map(_.values) match {
      case Some(countries) if countries.nonEmpty =>
        getCustomsSecurityAgreementAreaCountries()
          .map(_.values)
          .map(countries.filterNot(_ == countryOfDestination).intersect(_))
          .map(SelectableList(_))
      case _ => getCountries()
    }

  private def getCountries(listName: String)(implicit hc: HeaderCarrier): Future[SelectableList[Country]] =
    referenceDataConnector
      .getCountries(listName)
      .map(sort)

  def doesCountryRequireZip(country: Country)(implicit hc: HeaderCarrier): Future[Boolean] =
    getCountriesWithoutZip().map(!_.contains(country.code))

  private def sort(countries: Seq[Country]): SelectableList[Country] =
    SelectableList(countries.sortBy(_.description.toLowerCase))
}
