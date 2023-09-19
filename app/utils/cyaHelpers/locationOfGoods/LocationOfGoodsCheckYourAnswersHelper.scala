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

package utils.cyaHelpers.locationOfGoods

import config.FrontendAppConfig
import models.reference.{Country, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, Mode, PostalCodeAddress, UserAnswers}
import pages.locationOfGoods.contact.{NamePage, TelephoneNumberPage}
import pages.locationOfGoods._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class LocationOfGoodsCheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def addLocationOfGoods: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddLocationOfGoodsPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addLocationOfGoods",
    id = Some("change-add-location-of-goods")
  )

  def locationType: Option[SummaryListRow] = getAnswerAndBuildRow[LocationType](
    page = LocationTypePage,
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "locationOfGoods.locationType",
    id = Some("change-location-type")
  )

  def locationOfGoodsIdentification: Option[SummaryListRow] = getAnswerAndBuildRow[LocationOfGoodsIdentification](
    page = IdentificationPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.identification",
    id = Some("change-location-of-goods-identification")
  )

  def customsOfficeIdentifier: Option[SummaryListRow] = getAnswerAndBuildRow[CustomsOffice](
    page = CustomsOfficeIdentifierPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.customsOfficeIdentifier",
    id = Some("change-location-of-goods-customs-office-identifier")
  )

  def eori: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = EoriPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.eori",
    id = Some("change-location-of-goods-eori")
  )

  def authorisationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AuthorisationNumberPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.authorisationNumber",
    id = Some("change-location-of-goods-authorisation-number")
  )

  def coordinates: Option[SummaryListRow] = getAnswerAndBuildRow[Coordinates](
    page = CoordinatesPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.coordinates",
    id = Some("change-location-of-goods-coordinates")
  )

  def unLocode: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = UnLocodePage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.unLocode",
    id = Some("change-location-of-goods-un-locode")
  )

  def country: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "locationOfGoods.country",
    id = Some("change-location-of-goods-country")
  )

  def address: Option[SummaryListRow] = getAnswerAndBuildRow[DynamicAddress](
    page = AddressPage,
    formatAnswer = formatAsDynamicAddress,
    prefix = "locationOfGoods.address",
    id = Some("change-location-of-goods-address")
  )

  def postalCode: Option[SummaryListRow] = getAnswerAndBuildRow[PostalCodeAddress](
    page = PostalCodePage,
    formatAnswer = formatAsPostalCodeAddress,
    prefix = "locationOfGoods.postalCode",
    id = Some("change-location-of-goods-postal-code")
  )

  def additionalIdentifierYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentifierYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addIdentifierYesNo",
    id = Some("change-location-of-goods-add-identifier")
  )

  def additionalIdentifier: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = AdditionalIdentifierPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.additionalIdentifier",
    id = Some("change-location-of-goods-additional-identifier")
  )

  def contactYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddContactYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "locationOfGoods.addContactLocationOfGoods",
    id = Some("change-location-of-goods-add-contact")
  )

  def contactName: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = NamePage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.contact.name",
    id = Some("change-location-of-goods-contact")
  )

  def contactPhoneNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = TelephoneNumberPage,
    formatAnswer = formatAsText,
    prefix = "locationOfGoods.contact.telephoneNumber",
    id = Some("change-location-of-goods-contact-phone-number")
  )
}
