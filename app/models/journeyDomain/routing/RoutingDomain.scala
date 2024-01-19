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

package models.journeyDomain.routing

import config.PhaseConfig
import models.domain._
import models.journeyDomain.JourneyDomainModel
import models.reference.{Country, CustomsOffice}
import pages.routing.{BindingItineraryPage, CountryOfDestinationPage, OfficeOfDestinationPage}
import pages.sections.Section
import pages.sections.routing.RoutingSection

case class RoutingDomain(
  countryOfDestination: Country,
  officeOfDestination: CustomsOffice,
  bindingItinerary: Boolean,
  countriesOfRouting: CountriesOfRoutingDomain
) extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(RoutingSection)
}

object RoutingDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[RoutingDomain] =
    (
      CountryOfDestinationPage.reader,
      OfficeOfDestinationPage.reader,
      BindingItineraryPage.reader,
      CountriesOfRoutingDomain.userAnswersReader
    ).map(RoutingDomain.apply)
}
