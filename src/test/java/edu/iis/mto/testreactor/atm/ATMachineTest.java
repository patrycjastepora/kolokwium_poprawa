package edu.iis.mto.testreactor.atm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Currency;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {

    @Mock
    private Bank bank;

    private PinCode pinCode;
    private Card card;
    private Money money;
    private Money muchMoney;
    private Money wrongMoney;
    private Currency currency;

    private ATMachine atMachine;

    @BeforeEach
    void setUp() {
        Currency currency = Money.DEFAULT_CURRENCY;
        atMachine = new ATMachine(bank, currency);
        List<BanknotesPack> banknotesPacks = List.of(BanknotesPack.create(10, Banknote.PL_100), BanknotesPack.create(10, Banknote.PL_200));
        MoneyDeposit moneyDeposit = MoneyDeposit.create(currency, banknotesPacks);
        atMachine.setDeposit(moneyDeposit);
        pinCode = PinCode.createPIN(1,2,8,9);
        card = Card.create("123456789");
        money = new Money(100, currency);
        muchMoney = new Money(9000, currency);
        wrongMoney = new Money(92.999, currency);
    }

    @Test
    void shouldResultInSuccess() throws ATMOperationException {
        Withdrawal result = atMachine.withdraw(pinCode, card, money);
        List<Banknote> expectedBanknote = List.of(Banknote.PL_100, Banknote.PL_200);
        List<Banknote> resultBanknote = result.getBanknotes();

        assertEquals(resultBanknote, expectedBanknote);
    }

    @Test
    void hasNotEnoughMoney() {
        ATMOperationException exception = assertThrows(ATMOperationException.class, ()-> atMachine.withdraw(pinCode, card, muchMoney));
        assertEquals(exception.getErrorCode(), ErrorCode.WRONG_AMOUNT);
    }

    @Test
    void wrongAmountOfMoney() {
        ATMOperationException exception = assertThrows(ATMOperationException.class, ()-> atMachine.withdraw(pinCode,card, wrongMoney));
        assertEquals(exception.getErrorCode(),ErrorCode.WRONG_AMOUNT);
    }

    @Test
    void wrongAuthorization() throws AuthorizationException {
        doThrow(AuthorizationException.class).when(bank).autorize(any(), any());
        ATMOperationException errCode = assertThrows(ATMOperationException.class, () -> atMachine.withdraw(pinCode, card, money));
        assertEquals(errCode.getErrorCode(), ErrorCode.AUTHORIZATION);
    }

    @Test
    void wrongCurrency() {
        Money euroM = new Money(500, "EUR");
        Currency currency = Money.DEFAULT_CURRENCY;
        List<BanknotesPack> banknotesPacks = List.of(BanknotesPack.create(10, Banknote.PL_100), BanknotesPack.create(10, Banknote.PL_200));
        MoneyDeposit moneyDeposit = MoneyDeposit.create(currency, banknotesPacks);
        atMachine.setDeposit(moneyDeposit);
        ATMOperationException errCode = assertThrows(ATMOperationException.class, () -> atMachine.withdraw(pinCode, card, euroM));
        assertEquals(errCode.getErrorCode(), ErrorCode.WRONG_CURRENCY);
    }
    /*
    @Test
    void properBatchWithATM() throws AuthorizationException{
        InOrder callOrder = inOrder(bank, currency);
        callOrder.verify(bank).autorize(pinCode, card);
        callOrder.verify(bank).charge(token, money);

    }
    */
}
