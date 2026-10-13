import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavShellComponent } from './nav-shell.component';

describe('NavShellComponent', () => {
  let component: NavShellComponent;
  let fixture: ComponentFixture<NavShellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavShellComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavShellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
