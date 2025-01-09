import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditPostComponent } from './edit-post.component';
import { ReactiveFormsModule } from '@angular/forms';
import { PostService } from '../../../shared/services/post.service';
import { AuthService } from '../../../shared/services/auth.service';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Post } from '../../../shared/models/post.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import createSpyObj = jasmine.createSpyObj;

describe('EditPostComponent', () => {
  let component: EditPostComponent;
  let fixture: ComponentFixture<EditPostComponent>;

  // Mocks
  let postServiceMock: jasmine.SpyObj<PostService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;
  let paramMapMock: jasmine.SpyObj<ParamMap>;

  // Example Post object for test usage
  const mockPost: Post = {
    id: 5,
    title: 'Mock Title',
    content: 'Mock Content',
    status: 'DRAFT',
    author: 'testuser',
  };

  beforeEach(async () => {
    // Create spies for the methods we'll call
    postServiceMock = jasmine.createSpyObj<PostService>('PostService', [
      'getPostById',
      'updatePost',
    ]);
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', [
      'getRole',
      'getUsername',
    ]);
    routerMock = jasmine.createSpyObj<Router>('Router', ['navigate']);

    // Create a complete mock for ParamMap
    paramMapMock = jasmine.createSpyObj<ParamMap>(
      'ParamMap',
      ['has', 'getAll', 'get'],
      { keys: ['id'] }
    );
    paramMapMock.get.and.returnValue('5');
    paramMapMock.has.and.returnValue(true);
    paramMapMock.getAll.and.returnValue(['5']);

    // Mock the ActivatedRoute so that paramMap returns the above mock
    const activatedRouteMock: Partial<ActivatedRoute> = {
      snapshot: {
        paramMap: paramMapMock,
        url: [],
        params: {},
        queryParams: {},
        fragment: null,
        data: {},
        outlet: '',
        component: null,
        routeConfig: null,
        root: null,
        parent: null,
        firstChild: null,
        children: [],
        pathFromRoot: [],
        queryParamMap: {} as any,
      } as unknown as ActivatedRoute['snapshot'],
    };

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        // Since EditPostComponent is standalone, you can just import it directly:
        EditPostComponent,
      ],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(EditPostComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization and loadPost()', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should call loadPost() in ngOnInit and populate the form if user is author', () => {
      // Arrange
      const role = 'EDITOR';
      authServiceMock.getRole.and.returnValue(role);
      authServiceMock.getUsername.and.returnValue('testuser');

      // Our mock post is authored by 'testuser'
      postServiceMock.getPostById.and.returnValue(of(mockPost));

      // Act
      fixture.detectChanges(); // triggers ngOnInit

      // Assert
      expect(authServiceMock.getRole).toHaveBeenCalled();
      expect(authServiceMock.getUsername).toHaveBeenCalled();
      expect(postServiceMock.getPostById).toHaveBeenCalledWith(5);
      // Check that form was patched correctly
      expect(component.postForm.value.title).toBe('Mock Title');
      expect(component.postForm.value.content).toBe('Mock Content');
      // Since 'status' is not part of the form, it should not be patched
      // If 'status' should be part of the form, update the component accordingly

      // User is authorized, so isAuthorized should be true
      expect(component.isAuthorized).toBeTrue();
    });

    it('should navigate away if the user is not the post author', () => {
      // Arrange
      authServiceMock.getRole.and.returnValue('EDITOR');
      authServiceMock.getUsername.and.returnValue('anotheruser'); // Not the author
      postServiceMock.getPostById.and.returnValue(of(mockPost));

      // Act
      fixture.detectChanges();

      // Assert
      expect(postServiceMock.getPostById).toHaveBeenCalledWith(5);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/posts']);
      expect(component.isAuthorized).toBeFalse();
    });

    it('should handle error if getPostById fails', () => {
      // Arrange
      authServiceMock.getRole.and.returnValue('EDITOR');
      authServiceMock.getUsername.and.returnValue('testuser');
      postServiceMock.getPostById.and.returnValue(
        throwError(() => new Error('Load error'))
      );

      // Spy on console.error to confirm an error is logged
      spyOn(console, 'error');

      // Act
      fixture.detectChanges();

      // Assert
      expect(postServiceMock.getPostById).toHaveBeenCalledWith(5);
      expect(console.error).toHaveBeenCalledWith(
        'Failed to load post:',
        jasmine.any(Error)
      );
      // The form should remain with initial values
      expect(component.postForm.value.title).toBe('');
      expect(component.postForm.value.content).toBe('');
      expect(component.isAuthorized).toBeFalse();
    });
  });

  describe('updatePost()', () => {
    beforeEach(() => {
      // Before each test, let's pretend the user is authorized
      authServiceMock.getRole.and.returnValue('EDITOR');
      authServiceMock.getUsername.and.returnValue('testuser');
      postServiceMock.getPostById.and.returnValue(of(mockPost));
      // Trigger ngOnInit so the post is loaded
      fixture.detectChanges();
    });

    it('should not call updatePost if form is invalid', () => {
      // Clear out the title so the form is invalid
      component.postForm.controls['title'].setValue('');
      component.updatePost();
      expect(postServiceMock.updatePost).not.toHaveBeenCalled();
    });

    it('should handle error when updatePost fails', () => {
      // Fill in valid form
      component.postForm.setValue({
        title: 'Updated Title',
        content: 'Updated Content',
      });

      postServiceMock.updatePost.and.returnValue(
        throwError(() => new Error('Update error'))
      );
      spyOn(console, 'error');

      component.updatePost();

      // Check that an error was logged
      expect(console.error).toHaveBeenCalledWith(
        'Error updating post:',
        jasmine.any(Error)
      );
    });

    it('should not update post if user is not authorized', () => {
      // Suppose at this point the user is somehow deemed not authorized
      component.isAuthorized = false;
      component.postForm.setValue({
        title: 'Updated Title',
        content: 'Updated Content',
      });

      component.updatePost();
      expect(postServiceMock.updatePost).not.toHaveBeenCalled();
      expect(routerMock.navigate).not.toHaveBeenCalled();
    });
  });
});
