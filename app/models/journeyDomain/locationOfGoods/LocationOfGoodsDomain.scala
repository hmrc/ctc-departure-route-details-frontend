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

package models.journeyDomain.locationOfGoods

import config.Constants.LocationOfGoodsIdentifier._
import models._
import models.journeyDomain._
import models.reference.{Country, CustomsOffice}
import pages.locationOfGoods._
import pages.sections.Section
import pages.sections.locationOfGoods.LocationOfGoodsSection

sealed trait LocationOfGoodsDomain extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(LocationOfGoodsSection)

  val typeOfLocation: LocationType

  val additionalContact: Option[AdditionalContactDomain] = None
}

object LocationOfGoodsDomain {

  implicit val userAnswersReader: Read[LocationOfGoodsDomain] =
    UserAnswersReader.readInferred(LocationTypePage, InferredLocationTypePage).to {
      typeOfLocation =>
        UserAnswersReader.readInferred(IdentificationPage, InferredIdentificationPage).to {
          _.qualifier match {
            case CustomsOfficeIdentifier       => LocationOfGoodsV.userAnswersReader(typeOfLocation)
            case EoriNumberIdentifier          => LocationOfGoodsX.userAnswersReader(typeOfLocation)
            case AuthorisationNumberIdentifier => LocationOfGoodsY.userAnswersReader(typeOfLocation)
            case UnlocodeIdentifier            => LocationOfGoodsU.userAnswersReader(typeOfLocation)
            case CoordinatesIdentifier         => LocationOfGoodsW.userAnswersReader(typeOfLocation)
            case AddressIdentifier             => LocationOfGoodsZ.userAnswersReader(typeOfLocation)
            case PostalCodeIdentifier          => LocationOfGoodsT.userAnswersReader(typeOfLocation)
            case x                             => throw new Exception(s"Unexpected Location of goods identifier value $x")
          }
        }
    }

}

case class LocationOfGoodsV(
  typeOfLocation: LocationType,
  customsOffice: CustomsOffice
) extends LocationOfGoodsDomain

object LocationOfGoodsV {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CustomsOfficeIdentifierPage.reader
    ).map(LocationOfGoodsV.apply)
}

case class LocationOfGoodsX(
  typeOfLocation: LocationType,
  identificationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsX {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      EoriPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsX.apply)
}

case class LocationOfGoodsY(
  typeOfLocation: LocationType,
  authorisationNumber: String,
  additionalIdentifier: Option[String],
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsY {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      AuthorisationNumberPage.reader,
      AddIdentifierYesNoPage.filterOptionalDependent(identity)(AdditionalIdentifierPage.reader),
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsY.apply)
}

case class LocationOfGoodsW(
  typeOfLocation: LocationType,
  coordinates: Coordinates,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsW {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CoordinatesPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsW.apply)
}

case class LocationOfGoodsZ(
  typeOfLocation: LocationType,
  country: Country,
  address: DynamicAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsZ {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      CountryPage.reader,
      AddressPage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsZ.apply)
}

case class LocationOfGoodsU(
  typeOfLocation: LocationType,
  unLocode: String,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsU {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      UnLocodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsU.apply)
}

case class LocationOfGoodsT(
  typeOfLocation: LocationType,
  postalCodeAddress: PostalCodeAddress,
  override val additionalContact: Option[AdditionalContactDomain]
) extends LocationOfGoodsDomain

object LocationOfGoodsT {

  def userAnswersReader(typeOfLocation: LocationType): Read[LocationOfGoodsDomain] =
    (
      UserAnswersReader.success(typeOfLocation),
      PostalCodePage.reader,
      AddContactYesNoPage.filterOptionalDependent(identity)(AdditionalContactDomain.userAnswersReader)
    ).map(LocationOfGoodsT.apply)

}
