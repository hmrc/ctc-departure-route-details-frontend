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

package generators

import models.{Coordinates, DateTime, DeclarationType, DynamicAddress, LocationOfGoodsIdentification, LocationType, PostalCodeAddress, SecurityDetailsType}
import models.reference.{Country, CustomsOffice, UnLocode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.libs.json._
import queries.Gettable

trait UserAnswersEntryGenerators {
  self: Generators =>

  def generateAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = generateRouteDetailsAnswer

  private def generateRouteDetailsAnswer: PartialFunction[Gettable[_], Gen[JsValue]] =
    generateExternalAnswer orElse
      generateRoutingAnswer orElse
      generateTransitAnswer orElse
      generateExitAnswer orElse
      generateLocationOfGoodsAnswer orElse
      generateLoadingAndUnloadingAnswer

  private def generateExternalAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.external._
    {
      case OfficeOfDeparturePage   => arbitrary[CustomsOffice](arbitraryOfficeOfDeparture).map(Json.toJson(_))
      case DeclarationTypePage     => arbitrary[DeclarationType].map(Json.toJson(_))
      case SecurityDetailsTypePage => arbitrary[SecurityDetailsType].map(Json.toJson(_))
    }
  }

  private def generateRoutingAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.routing._
    import pages.routing.index._
    {
      case CountryOfDestinationPage     => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfDestinationPage      => arbitrary[CustomsOffice].map(Json.toJson(_))
      case BindingItineraryPage         => arbitrary[Boolean].map(JsBoolean)
      case AddCountryOfRoutingYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case CountryOfRoutingPage(_)      => arbitrary[Country].map(Json.toJson(_))
    }
  }

  private def generateTransitAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.transit._
    import pages.transit.index._
    {
      case T2DeclarationTypeYesNoPage        => arbitrary[Boolean].map(JsBoolean)
      case AddOfficeOfTransitYesNoPage       => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfTransitCountryPage(_)     => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfTransitPage(_)            => arbitrary[CustomsOffice].map(Json.toJson(_))
      case AddOfficeOfTransitETAYesNoPage(_) => arbitrary[Boolean].map(JsBoolean)
      case OfficeOfTransitETAPage(_)         => arbitrary[DateTime].map(Json.toJson(_))
    }
  }

  private def generateExitAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.exit.index._
    {
      case OfficeOfExitCountryPage(_) => arbitrary[Country].map(Json.toJson(_))
      case OfficeOfExitPage(_)        => arbitrary[CustomsOffice].map(Json.toJson(_))
    }
  }

  private def generateLocationOfGoodsAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.locationOfGoods._
    {
      val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
        case AddLocationOfGoodsPage => arbitrary[Boolean].map(JsBoolean)
        case LocationTypePage       => arbitrary[LocationType].map(Json.toJson(_))
        case IdentificationPage     => arbitrary[LocationOfGoodsIdentification].map(Json.toJson(_))
        case AddContactYesNoPage    => arbitrary[Boolean].map(JsBoolean)
      }

      pf orElse
        generateLocationOfGoodsIdentifierAnswer orElse
        generateLocationOfGoodsContactAnswer
    }
  }

  private def generateLocationOfGoodsIdentifierAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.locationOfGoods._
    {
      case CustomsOfficeIdentifierPage => arbitrary[CustomsOffice].map(Json.toJson(_))
      case EoriPage                    => Gen.alphaNumStr.map(JsString)
      case AuthorisationNumberPage     => Gen.alphaNumStr.map(JsString)
      case AddIdentifierYesNoPage      => arbitrary[Boolean].map(JsBoolean)
      case AdditionalIdentifierPage    => Gen.alphaNumStr.map(JsString)
      case CoordinatesPage             => arbitrary[Coordinates].map(Json.toJson(_))
      case UnLocodePage                => arbitrary[UnLocode].map(Json.toJson(_))
      case CountryPage                 => arbitrary[Country].map(Json.toJson(_))
      case AddressPage                 => arbitrary[DynamicAddress].map(Json.toJson(_))
      case PostalCodePage              => arbitrary[PostalCodeAddress].map(Json.toJson(_))
    }
  }

  private def generateLocationOfGoodsContactAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.locationOfGoods.contact._
    {
      case NamePage            => Gen.alphaNumStr.map(JsString)
      case TelephoneNumberPage => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateLoadingAndUnloadingAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.loadingAndUnloading._
    {
      val pf: PartialFunction[Gettable[_], Gen[JsValue]] = {
        case AddPlaceOfUnloadingPage => arbitrary[Boolean].map(JsBoolean)
      }

      generateLoadingAnswer orElse
        pf orElse
        generateUnloadingAnswer
    }
  }

  private def generateLoadingAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.loadingAndUnloading.loading._
    {
      case AddUnLocodeYesNoPage         => arbitrary[Boolean].map(JsBoolean)
      case UnLocodePage                 => arbitrary[UnLocode].map(Json.toJson(_))
      case AddExtraInformationYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case CountryPage                  => arbitrary[Country].map(Json.toJson(_))
      case LocationPage                 => Gen.alphaNumStr.map(JsString)
    }
  }

  private def generateUnloadingAnswer: PartialFunction[Gettable[_], Gen[JsValue]] = {
    import pages.loadingAndUnloading.AddPlaceOfUnloadingPage
    import pages.loadingAndUnloading.unloading._
    {
      case AddPlaceOfUnloadingPage      => arbitrary[Boolean].map(JsBoolean)
      case UnLocodeYesNoPage            => arbitrary[Boolean].map(JsBoolean)
      case UnLocodePage                 => arbitrary[UnLocode].map(Json.toJson(_))
      case AddExtraInformationYesNoPage => arbitrary[Boolean].map(JsBoolean)
      case CountryPage                  => arbitrary[Country].map(Json.toJson(_))
      case LocationPage                 => Gen.alphaNumStr.map(JsString)
    }
  }
}
