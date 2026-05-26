package com.group4.swissrouteapi;

import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Mock of creation of search history records. */
public class SearchHistoryMockFactory {

  private static final Random RANDOM = new Random();

  private static final List<String> SWISS_CANTONS =
      List.of(
          "Aargau",
          "Appenzell Ausserrhoden",
          "Appenzell Innerrhoden",
          "Basilea-Ciudad",
          "Basilea-Campiña",
          "Berna",
          "Friburgo",
          "Ginebra",
          "Glaris",
          "Grisones",
          "Jura",
          "Lucerna",
          "Neuchâtel",
          "Nidwalden",
          "Obwalden",
          "San Galo",
          "Schaffhausen",
          "Schwyz",
          "Soleura",
          "Tesino",
          "Turgovia",
          "Uri",
          "Valais",
          "Vaud",
          "Zug",
          "Zúrich");

  /**
   * Creates a mock {@link SearchHistoryEntity} for the given user.
   *
   * <p>Origin and destination are selected randomly from Swiss cantons. Origin and destination can
   * never be the same. resultCount is random between 1 and 15.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static SearchHistoryEntity createMockHistory(UserEntity user) {

    String origin = getRandomCanton();
    String destination;

    do {
      destination = getRandomCanton();
    } while (origin.equals(destination));

    return SearchHistoryEntity.builder()
        .user(user)
        .origin(origin)
        .destination(destination)
        .resultCount(getRandomResultCount())
        .build();
  }

  /**
   * Creates a list of mock {@link SearchHistoryEntity}.
   *
   * @param user the owning {@link UserEntity}
   * @param amount number of entities to generate
   * @return list of mock entities
   */
  public static List<SearchHistoryEntity> createMockHistories(UserEntity user, int amount) {

    List<SearchHistoryEntity> histories = new ArrayList<>();

    for (int i = 0; i < amount; i++) {
      histories.add(createMockHistory(user));
    }

    return histories;
  }

  private static String getRandomCanton() {
    return SWISS_CANTONS.get(RANDOM.nextInt(SWISS_CANTONS.size()));
  }

  private static int getRandomResultCount() {
    return RANDOM.nextInt(15) + 1;
  }
}
