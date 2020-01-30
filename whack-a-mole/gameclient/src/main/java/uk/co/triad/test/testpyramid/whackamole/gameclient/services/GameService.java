package uk.co.triad.test.testpyramid.whackamole.gameclient.services;

import uk.co.triad.test.testpyramid.whackamole.gameclient.domain.Mole;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface GameService
{
  CompletionStage<Void> register(String playerName);

  CompletionStage<Void> deregister(String playerName);

  CompletionStage<List<Mole>> activeMoles();

  CompletionStage<Boolean> whack(String playerName, Mole mole);
}
