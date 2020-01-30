package uk.co.triad.test.testpyramid.whackamole.gameserver.core.domain;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MoleTest
{
  @Test
  public void shouldReturnNewMoleWithPopulatedId()
  {
    assertNotNull(Mole.newMole().getId());
  }

  @Test
  public void shouldReturnHashCode()
  {
    var moleId = UUID.randomUUID();
    var mole = Mole.newMole(moleId);

    assertEquals(moleId.hashCode(), mole.hashCode());
  }

  @Test
  public void shouldBeEqualWhenIdIsEqual() throws NoSuchFieldException, IllegalAccessException
  {
    var mole1 = Mole.newMole();
    var mole2 = Mole.newMole();

    var fieldId = mole2.getClass().getDeclaredField("id");
    fieldId.setAccessible(true);

    assertNotEquals(mole1, mole2);
    fieldId.set(mole2, mole1.getId());

    assertEquals(mole1, mole2);
  }

  @Test
  public void shouldBeUnequalWhenDifferentObject()
  {
    var mole = Mole.newMole();
    assertNotEquals(mole, new Object());
  }
}
