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

package utils.cyaHelpers

import base.SpecBase
import controllers.loadingAndUnloading.loading.{routes => loadingRoutes}
import controllers.loadingAndUnloading.unloading.{routes => unloadingRoutes}
import controllers.loadingAndUnloading.{routes => loadingAndUnloadingRoutes}
import pages.loadingAndUnloading._
import generators.Generators
import models.Mode
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loadingAndUnloading.{loading, unloading, AddPlaceOfUnloadingPage}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.cyaHelpers.loadingAndUnloading.LoadingAndUnloadingCheckYourAnswersHelper

class LoadingAndUnloadingCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloadingCheckYourAnswersHelper" - {

    "addLoading" - {
      "must return None" - {
        "when AddLoadingYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addLoading
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddLoadingYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddPlaceOfLoadingYesNoPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addLoading

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a place of loading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingAndUnloadingRoutes.AddPlaceOfLoadingYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a place of loading"),
                          attributes = Map("id" -> "change-add-loading")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addLoadingUnLocode" - {
      "must return None" - {
        "when AddUnLocodeYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addLoadingUnLocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddUnLocodeYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(loading.AddUnLocodeYesNoPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addLoadingUnLocode

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a UN/LOCODE for the place of loading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingRoutes.AddUnLocodeYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want a UN/LOCODE for the place of loading"),
                          attributes = Map("id" -> "change-add-loading-un-locode")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "loadingUnLocode" - {
      "must return None" - {
        "when UnLocodePage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.loadingUnLocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when UnLocodePage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, unLocode) =>
              val answers = emptyUserAnswers.setValue(loading.UnLocodePage, unLocode)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.loadingUnLocode

              result.value mustEqual
                SummaryListRow(
                  key = Key("UN/LOCODE".toText),
                  value = Value(unLocode.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingRoutes.UnLocodeController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("UN/LOCODE for the place of loading"),
                          attributes = Map("id" -> "change-loading-un-locode")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addLoadingCountryAndLocation" - {
      "must return None" - {
        "when AddExtraInformationYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addLoadingCountryAndLocation
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddExtraInformationYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(loading.AddExtraInformationYesNoPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addLoadingCountryAndLocation

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add extra information for the place of loading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingRoutes.AddExtraInformationYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add extra information for the place of loading"),
                          attributes = Map("id" -> "change-add-loading-country-and-location")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "loadingCountry" - {
      "must return None" - {
        "when CountryPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.loadingCountry
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when CountryPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val answers = emptyUserAnswers.setValue(loading.CountryPage, country)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.loadingCountry

              result.value mustEqual
                SummaryListRow(
                  key = Key("Country".toText),
                  value = Value(country.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingRoutes.CountryController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("country for the place of loading"),
                          attributes = Map("id" -> "change-loading-country")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "loadingLocation" - {
      "must return None" - {
        "when LocationPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.loadingLocation
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when LocationPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, location) =>
              val answers = emptyUserAnswers.setValue(loading.LocationPage, location)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.loadingLocation

              result.value mustEqual
                SummaryListRow(
                  key = Key("Location".toText),
                  value = Value(location.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingRoutes.LocationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("location for the place of loading"),
                          attributes = Map("id" -> "change-loading-location")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addPlaceOfUnloading" - {
      "must return None" - {
        "when AddPlaceOfUnloadingPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addPlaceOfUnloading
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddPlaceOfUnloadingPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddPlaceOfUnloadingPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addPlaceOfUnloading

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a place of unloading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = loadingAndUnloadingRoutes.AddPlaceOfUnloadingController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a place of unloading"),
                          attributes = Map("id" -> "change-add-place-of-unloading")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addUnloadingUnLocode" - {
      "must return None" - {
        "when AddUnLocodeYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addUnloadingUnLocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddUnLocodeYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(unloading.UnLocodeYesNoPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addUnloadingUnLocode

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a UN/LOCODE for the place of unloading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = unloadingRoutes.AddUnLocodeYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a UN/LOCODE for the place of unloading"),
                          attributes = Map("id" -> "change-add-unloading-un-locode")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "unloadingUnLocode" - {
      "must return None" - {
        "when UnLocodePage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.unloadingUnLocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when UnLocodePage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, unLocode) =>
              val answers = emptyUserAnswers.setValue(unloading.UnLocodePage, unLocode)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.unloadingUnLocode

              result.value mustEqual
                SummaryListRow(
                  key = Key("UN/LOCODE".toText),
                  value = Value(unLocode.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = unloadingRoutes.UnLocodeController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("UN/LOCODE for the place of unloading"),
                          attributes = Map("id" -> "change-unloading-un-locode")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addUnloadingCountryAndLocation" - {
      "must return None" - {
        "when AddExtraInformationYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addUnloadingCountryAndLocation
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddExtraInformationYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(unloading.AddExtraInformationYesNoPage, true)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addUnloadingCountryAndLocation

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add extra information for the place of unloading?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = unloadingRoutes.AddExtraInformationYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add extra information for the place of unloading"),
                          attributes = Map("id" -> "change-add-unloading-country-and-location")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "unloadingCountry" - {
      "must return None" - {
        "when CountryPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.unloadingCountry
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when CountryPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val answers = emptyUserAnswers.setValue(unloading.CountryPage, country)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.unloadingCountry

              result.value mustEqual
                SummaryListRow(
                  key = Key("Country".toText),
                  value = Value(country.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = unloadingRoutes.CountryController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("country for the place of unloading"),
                          attributes = Map("id" -> "change-unloading-country")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "unloadingLocation" - {
      "must return None" - {
        "when PlaceOfUnloadingLocationPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LoadingAndUnloadingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.unloadingLocation
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when PlaceOfUnloadingLocationPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, location) =>
              val answers = emptyUserAnswers.setValue(unloading.LocationPage, location)
              val helper  = new LoadingAndUnloadingCheckYourAnswersHelper(answers, mode)
              val result  = helper.unloadingLocation

              result.value mustEqual
                SummaryListRow(
                  key = Key("Location".toText),
                  value = Value(location.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = unloadingRoutes.LocationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("location for the place of unloading"),
                          attributes = Map("id" -> "change-unloading-location")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }
  }
}
